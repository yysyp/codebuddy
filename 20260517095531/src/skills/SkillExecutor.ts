import { Logger } from '../utils/Logger';
import { Skill, SkillContext, SkillResult } from './types/Skill';

export interface ExecutionPlan {
    skillId: string;
    order: number;
    dependencies: string[];
}

export interface ExecutionResult {
    plan: ExecutionPlan[];
    results: Map<string, SkillResult>;
    totalTime: number;
    success: boolean;
}

export class SkillExecutor {
    private readonly logger: Logger;
    private readonly executingSkills: Set<string>;
    
    constructor() {
        this.logger = Logger.getInstance('SkillExecutor');
        this.executingSkills = new Set<string>();
    }
    
    public async execute(
        skill: Skill,
        context: SkillContext,
        timeout: number = 30000
    ): Promise<SkillResult> {
        if (this.executingSkills.has(skill.id)) {
            return {
                success: false,
                message: `Skill ${skill.id} is already executing`
            };
        }
        
        this.executingSkills.add(skill.id);
        const startTime = Date.now();
        
        try {
            this.logger.info(`Executing skill: ${skill.id}`);
            
            const result = await Promise.race([
                skill.execute(context),
                this.createTimeoutPromise(timeout)
            ]);
            
            this.logger.info(`Skill ${skill.id} executed successfully`, {
                duration: Date.now() - startTime,
                success: result.success
            });
            
            return result;
        } catch (error) {
            this.logger.error(`Skill ${skill.id} execution failed`, error);
            
            return {
                success: false,
                message: `Execution failed: ${(error as Error).message}`,
                errors: [{
                    field: 'execution',
                    code: 'EXECUTION_ERROR',
                    message: (error as Error).message
                }]
            };
        } finally {
            this.executingSkills.delete(skill.id);
        }
    }
    
    public async executeSequence(
        skills: Skill[],
        context: SkillContext,
        timeout: number = 60000
    ): Promise<ExecutionResult> {
        const plan = this.createExecutionPlan(skills);
        const results = new Map<string, SkillResult>();
        const startTime = Date.now();
        let success = true;
        
        for (const step of plan) {
            const skill = skills.find(s => s.id === step.skillId);
            if (!skill) {
                results.set(step.skillId, {
                    success: false,
                    message: `Skill ${step.skillId} not found`
                });
                success = false;
                continue;
            }
            
            if (step.dependencies.length > 0) {
                const dependencyResults = step.dependencies.map(dep => results.get(dep));
                if (dependencyResults.some(r => !r?.success)) {
                    results.set(step.skillId, {
                        success: false,
                        message: 'Dependencies failed'
                    });
                    success = false;
                    continue;
                }
            }
            
            const result = await this.execute(skill, context, timeout / plan.length);
            results.set(step.skillId, result);
            
            if (!result.success) {
                success = false;
            }
        }
        
        return {
            plan,
            results,
            totalTime: Date.now() - startTime,
            success
        };
    }
    
    public async executeParallel(
        skills: Skill[],
        context: SkillContext,
        timeout: number = 60000
    ): Promise<ExecutionResult> {
        const plan = this.createExecutionPlan(skills);
        const results = new Map<string, SkillResult>();
        const startTime = Date.now();
        
        const executions = skills.map(async (skill) => {
            const result = await this.execute(skill, context, timeout);
            results.set(skill.id, result);
            return result;
        });
        
        await Promise.all(executions);
        
        const success = Array.from(results.values()).every(r => r.success);
        
        return {
            plan,
            results,
            totalTime: Date.now() - startTime,
            success
        };
    }
    
    private createExecutionPlan(skills: Skill[]): ExecutionPlan[] {
        return skills.map((skill, index) => ({
            skillId: skill.id,
            order: index,
            dependencies: []
        }));
    }
    
    private createTimeoutPromise(timeout: number): Promise<never> {
        return new Promise((_, reject) => {
            setTimeout(() => {
                reject(new Error(`Execution timeout after ${timeout}ms`));
            }, timeout);
        });
    }
    
    public isExecuting(skillId: string): boolean {
        return this.executingSkills.has(skillId);
    }
    
    public getExecutingCount(): number {
        return this.executingSkills.size;
    }
    
    public dispose(): void {
        this.executingSkills.clear();
    }
}
