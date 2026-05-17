import { Logger } from '../utils/Logger';
import { Skill, SkillContext, SkillResult, SkillMetadata, SkillState, SkillPerformance } from './types/Skill';
import { SkillLoader } from './SkillLoader';
import { SkillEvolution } from './SkillEvolution';

export interface RegisteredSkill {
    skill: Skill;
    state: SkillState;
    config: SkillConfig;
    performance: SkillPerformance;
}

export interface SkillConfig {
    enabled: boolean;
    priority: number;
    timeout: number;
    retries: number;
}

export class SkillRegistry {
    private static instance: SkillRegistry | null = null;
    
    private readonly logger: Logger;
    private readonly skills: Map<string, RegisteredSkill>;
    private readonly skillLoader: SkillLoader;
    private readonly skillEvolution: SkillEvolution;
    private isInitialized: boolean = false;
    
    private constructor() {
        this.logger = Logger.getInstance('SkillRegistry');
        this.skills = new Map<string, RegisteredSkill>();
        this.skillLoader = new SkillLoader();
        this.skillEvolution = SkillEvolution.getInstance();
    }
    
    public static getInstance(): SkillRegistry {
        if (!SkillRegistry.instance) {
            SkillRegistry.instance = new SkillRegistry();
        }
        return SkillRegistry.instance;
    }
    
    public async initialize(): Promise<void> {
        if (this.isInitialized) {
            this.logger.warn('SkillRegistry already initialized');
            return;
        }
        
        this.logger.info('Initializing SkillRegistry...');
        
        const loadedSkills = await this.skillLoader.loadAllSkills();
        
        for (const skill of loadedSkills) {
            await this.register(skill);
        }
        
        this.isInitialized = true;
        this.logger.info('SkillRegistry initialized', { skillCount: this.skills.size });
    }
    
    public async register(skill: Skill): Promise<void> {
        if (this.skills.has(skill.id)) {
            this.logger.warn(`Skill ${skill.id} already registered, updating...`);
            await this.unregister(skill.id);
        }
        
        const isValid = await skill.validate();
        if (!isValid) {
            throw new Error(`Skill ${skill.id} failed validation`);
        }
        
        const registeredSkill: RegisteredSkill = {
            skill,
            state: 'ready',
            config: {
                enabled: true,
                priority: 100,
                timeout: 30000,
                retries: 3
            },
            performance: {
                skillId: skill.id,
                totalExecutions: 0,
                successfulExecutions: 0,
                failedExecutions: 0,
                averageExecutionTime: 0,
                lastExecuted: new Date(),
                successRate: 0
            }
        };
        
        this.skills.set(skill.id, registeredSkill);
        this.logger.info(`Registered skill: ${skill.id} (${skill.name})`);
    }
    
    public async unregister(skillId: string): Promise<void> {
        const skill = this.skills.get(skillId);
        if (skill) {
            this.skills.delete(skillId);
            this.logger.info(`Unregistered skill: ${skillId}`);
        }
    }
    
    public async findSkills(intent: { type: string; entities?: Array<{ type: string; value: string }> }): Promise<Skill[]> {
        const matchingSkills: Array<{ skill: Skill; score: number }> = [];
        
        for (const [skillId, registeredSkill] of this.skills.entries()) {
            if (registeredSkill.state !== 'ready' || !registeredSkill.config.enabled) {
                continue;
            }
            
            let score = 0;
            
            if (registeredSkill.skill.supportedIntents.includes(intent.type)) {
                score += 50;
            }
            
            if (intent.entities) {
                for (const entity of intent.entities) {
                    if (registeredSkill.skill.tags.includes(entity.type)) {
                        score += 10;
                    }
                }
            }
            
            score += registeredSkill.config.priority;
            
            score += registeredSkill.performance.successRate * 20;
            
            if (score > 0) {
                matchingSkills.push({ skill: registeredSkill.skill, score });
            }
        }
        
        matchingSkills.sort((a, b) => b.score - a.score);
        
        return matchingSkills.map(m => m.skill);
    }
    
    public async executeSkill(
        skillId: string,
        context: SkillContext
    ): Promise<SkillResult> {
        const registeredSkill = this.skills.get(skillId);
        if (!registeredSkill) {
            return {
                success: false,
                message: `Skill ${skillId} not found`
            };
        }
        
        if (registeredSkill.state !== 'ready') {
            return {
                success: false,
                message: `Skill ${skillId} is not ready (state: ${registeredSkill.state})`
            };
        }
        
        if (!registeredSkill.config.enabled) {
            return {
                success: false,
                message: `Skill ${skillId} is disabled`
            };
        }
        
        registeredSkill.state = 'executing';
        const startTime = Date.now();
        
        try {
            const result = await this.executeWithTimeout(
                registeredSkill.skill,
                context,
                registeredSkill.config.timeout
            );
            
            registeredSkill.performance.totalExecutions++;
            registeredSkill.performance.successfulExecutions++;
            registeredSkill.performance.lastExecuted = new Date();
            registeredSkill.performance.averageExecutionTime = 
                (registeredSkill.performance.averageExecutionTime * 
                    (registeredSkill.performance.totalExecutions - 1) + 
                    (Date.now() - startTime)) / 
                registeredSkill.performance.totalExecutions;
            registeredSkill.performance.successRate = 
                registeredSkill.performance.successfulExecutions / 
                registeredSkill.performance.totalExecutions;
            
            registeredSkill.state = 'ready';
            
            await this.skillEvolution.recordExecution(skillId, context, result);
            
            return result;
        } catch (error) {
            registeredSkill.performance.totalExecutions++;
            registeredSkill.performance.failedExecutions++;
            registeredSkill.performance.lastExecuted = new Date();
            registeredSkill.state = 'error';
            
            this.logger.error(`Skill ${skillId} execution failed`, error);
            
            if (registeredSkill.config.retries > 0) {
                return this.executeSkillWithRetry(
                    skillId,
                    context,
                    registeredSkill.config.retries - 1
                );
            }
            
            return {
                success: false,
                message: `Skill ${skillId} execution failed: ${(error as Error).message}`
            };
        }
    }
    
    private async executeWithTimeout(
        skill: Skill,
        context: SkillContext,
        timeout: number
    ): Promise<SkillResult> {
        return Promise.race([
            skill.execute(context),
            new Promise<SkillResult>((_, reject) =>
                setTimeout(() => reject(new Error('Skill execution timeout')), timeout)
            )
        ]);
    }
    
    private async executeSkillWithRetry(
        skillId: string,
        context: SkillContext,
        retries: number
    ): Promise<SkillResult> {
        this.logger.info(`Retrying skill ${skillId}, ${retries} retries left`);
        
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        return this.executeSkill(skillId, context);
    }
    
    public getAllSkills(): Skill[] {
        return Array.from(this.skills.values())
            .filter(rs => rs.config.enabled)
            .map(rs => rs.skill);
    }
    
    public getSkill(skillId: string): Skill | undefined {
        return this.skills.get(skillId)?.skill;
    }
    
    public getSkillPerformance(skillId: string): SkillPerformance | undefined {
        return this.skills.get(skillId)?.performance;
    }
    
    public getSkillMetadata(skillId: string): SkillMetadata | undefined {
        const skill = this.skills.get(skillId)?.skill;
        if (!skill) {
            return undefined;
        }
        
        const performance = this.skills.get(skillId)!.performance;
        
        return {
            ...skill.getMetadata(),
            usageCount: performance.totalExecutions,
            successRate: performance.successRate
        };
    }
    
    public setSkillEnabled(skillId: string, enabled: boolean): void {
        const skill = this.skills.get(skillId);
        if (skill) {
            skill.config.enabled = enabled;
            this.logger.info(`Skill ${skillId} ${enabled ? 'enabled' : 'disabled'}`);
        }
    }
    
    public setSkillPriority(skillId: string, priority: number): void {
        const skill = this.skills.get(skillId);
        if (skill) {
            skill.config.priority = priority;
            this.logger.info(`Skill ${skillId} priority set to ${priority}`);
        }
    }
    
    public async updateSkill(skillId: string, newSkill: Skill): Promise<void> {
        if (!this.skills.has(skillId)) {
            throw new Error(`Skill ${skillId} not found`);
        }
        
        await this.unregister(skillId);
        await this.register(newSkill);
        
        this.logger.info(`Skill ${skillId} updated to version ${newSkill.version}`);
    }
    
    public getStatistics() {
        const skills = Array.from(this.skills.values());
        
        let totalExecutions = 0;
        let totalSuccessful = 0;
        
        for (const skill of skills) {
            totalExecutions += skill.performance.totalExecutions;
            totalSuccessful += skill.performance.successfulExecutions;
        }
        
        const topPerforming = skills
            .map(s => s.performance)
            .sort((a, b) => b.successRate - a.successRate)
            .slice(0, 5);
        
        return {
            totalSkills: skills.length,
            activeSkills: skills.filter(s => s.config.enabled && s.state === 'ready').length,
            totalExecutions,
            averageSuccessRate: totalExecutions > 0 ? totalSuccessful / totalExecutions : 0,
            topPerformingSkills: topPerforming
        };
    }
    
    public dispose(): void {
        this.skills.clear();
        this.isInitialized = false;
        SkillRegistry.instance = null;
        this.logger.info('SkillRegistry disposed');
    }
}
