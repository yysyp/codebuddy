import { Logger } from '../utils/Logger';
import { Skill, SkillContext, SkillResult } from './types/Skill';
import { SkillRegistry } from './SkillRegistry';
import { KnowledgeBase } from '../learning/KnowledgeBase';

export interface EvolutionConfig {
    enabled: boolean;
    learningRate: number;
    maxIterations: number;
    convergenceThreshold: number;
}

export interface EvolutionRecord {
    skillId: string;
    context: SkillContext;
    result: SkillResult;
    timestamp: Date;
    feedback?: number;
}

export interface SkillEvolutionState {
    skillId: string;
    version: number;
    performance: number;
    history: EvolutionRecord[];
    optimizations: Optimization[];
}

export interface Optimization {
    type: string;
    description: string;
    improvement: number;
    timestamp: Date;
}

export class SkillEvolution {
    private static instance: SkillEvolution | null = null;
    
    private readonly logger: Logger;
    private readonly evolutionRecords: Map<string, EvolutionRecord[]>;
    private readonly evolutionStates: Map<string, SkillEvolutionState>;
    private readonly config: EvolutionConfig;
    private readonly knowledgeBase: KnowledgeBase;
    private readonly skillRegistry: SkillRegistry;
    
    private constructor() {
        this.logger = Logger.getInstance('SkillEvolution');
        this.evolutionRecords = new Map<string, EvolutionRecord[]>();
        this.evolutionStates = new Map<string, SkillEvolutionState>();
        this.knowledgeBase = KnowledgeBase.getInstance();
        this.skillRegistry = SkillRegistry.getInstance();
        
        this.config = {
            enabled: true,
            learningRate: 0.1,
            maxIterations: 100,
            convergenceThreshold: 0.01
        };
    }
    
    public static getInstance(): SkillEvolution {
        if (!SkillEvolution.instance) {
            SkillEvolution.instance = new SkillEvolution();
        }
        return SkillEvolution.instance;
    }
    
    public async initialize(): Promise<void> {
        this.logger.info('Initializing SkillEvolution...');
        await this.knowledgeBase.initialize();
        this.logger.info('SkillEvolution initialized');
    }
    
    public async recordExecution(
        skillId: string,
        context: SkillContext,
        result: SkillResult
    ): Promise<void> {
        const record: EvolutionRecord = {
            skillId,
            context,
            result,
            timestamp: new Date()
        };
        
        const records = this.evolutionRecords.get(skillId) || [];
        records.push(record);
        
        if (records.length > 1000) {
            records.shift();
        }
        
        this.evolutionRecords.set(skillId, records);
        
        await this.knowledgeBase.addEntry({
            type: 'skill_execution',
            skillId,
            context,
            result,
            timestamp: new Date()
        });
        
        this.logger.debug(`Recorded execution for skill ${skillId}`);
    }
    
    public async addFeedback(
        skillId: string,
        feedback: number,
        context?: SkillContext
    ): Promise<void> {
        const records = this.evolutionRecords.get(skillId);
        if (records && records.length > 0) {
            const latestRecord = records[records.length - 1];
            latestRecord.feedback = feedback;
            
            await this.knowledgeBase.addEntry({
                type: 'skill_feedback',
                skillId,
                feedback,
                context: context || latestRecord.context,
                timestamp: new Date()
            });
            
            this.logger.debug(`Added feedback ${feedback} for skill ${skillId}`);
        }
    }
    
    public async evolve(skillId: string): Promise<Optimization | null> {
        if (!this.config.enabled) {
            return null;
        }
        
        const records = this.evolutionRecords.get(skillId);
        if (!records || records.length < 10) {
            this.logger.debug(`Not enough data to evolve skill ${skillId}`);
            return null;
        }
        
        const pattern = this.analyzePatterns(records);
        
        if (pattern) {
            const optimization = await this.applyOptimization(skillId, pattern);
            
            if (optimization) {
                await this.recordOptimization(skillId, optimization);
                return optimization;
            }
        }
        
        return null;
    }
    
    private analyzePatterns(records: EvolutionRecord[]): Record<string, unknown> | null {
        const successfulRecords = records.filter(r => r.result.success);
        const failedRecords = records.filter(r => !r.result.success);
        
        if (successfulRecords.length === 0) {
            return null;
        }
        
        const successRate = successfulRecords.length / records.length;
        
        const intentCounts = new Map<string, number>();
        for (const record of records) {
            const intent = record.context.intent.type;
            intentCounts.set(intent, (intentCounts.get(intent) || 0) + 1);
        }
        
        const mostCommonIntent = [...intentCounts.entries()]
            .sort((a, b) => b[1] - a[1])[0]?.[0];
        
        const languageCounts = new Map<string, number>();
        for (const record of records) {
            const lang = record.context.context?.languageId as string || 'unknown';
            languageCounts.set(lang, (languageCounts.get(lang) || 0) + 1);
        }
        
        const mostCommonLanguage = [...languageCounts.entries()]
            .sort((a, b) => b[1] - a[1])[0]?.[0];
        
        return {
            successRate,
            totalExecutions: records.length,
            mostCommonIntent,
            mostCommonLanguage,
            avgFeedback: this.calculateAverageFeedback(records)
        };
    }
    
    private calculateAverageFeedback(records: EvolutionRecord[]): number {
        const withFeedback = records.filter(r => r.feedback !== undefined);
        if (withFeedback.length === 0) {
            return 0.5;
        }
        
        const sum = withFeedback.reduce((acc, r) => acc + (r.feedback || 0), 0);
        return sum / withFeedback.length;
    }
    
    private async applyOptimization(
        skillId: string,
        pattern: Record<string, unknown>
    ): Promise<Optimization | null> {
        const optimizationType = this.determineOptimizationType(pattern);
        
        if (!optimizationType) {
            return null;
        }
        
        const optimization: Optimization = {
            type: optimizationType.type,
            description: optimizationType.description,
            improvement: this.calculateImprovement(pattern),
            timestamp: new Date()
        };
        
        this.logger.info(`Applying optimization to skill ${skillId}: ${optimizationType.description}`);
        
        return optimization;
    }
    
    private determineOptimizationType(
        pattern: Record<string, unknown>
    ): { type: string; description: string } | null {
        const successRate = pattern.successRate as number;
        const avgFeedback = pattern.avgFeedback as number;
        
        if (successRate < 0.7 || avgFeedback < 0.5) {
            return {
                type: 'improve_accuracy',
                description: 'Improve skill accuracy by adjusting parameters'
            };
        }
        
        if (successRate > 0.9 && avgFeedback > 0.8) {
            return {
                type: 'increase_confidence',
                description: 'Increase confidence threshold for better precision'
            };
        }
        
        return {
            type: 'tune_parameters',
            description: 'Fine-tune skill parameters based on usage patterns'
        };
    }
    
    private calculateImprovement(pattern: Record<string, unknown>): number {
        const successRate = pattern.successRate as number;
        const avgFeedback = pattern.avgFeedback as number;
        
        return (successRate + avgFeedback) / 2 * this.config.learningRate;
    }
    
    private async recordOptimization(
        skillId: string,
        optimization: Optimization
    ): Promise<void> {
        let state = this.evolutionStates.get(skillId);
        
        if (!state) {
            state = {
                skillId,
                version: 1,
                performance: 0,
                history: [],
                optimizations: []
            };
        }
        
        state.version++;
        state.performance += optimization.improvement;
        state.optimizations.push(optimization);
        
        this.evolutionStates.set(skillId, state);
        
        await this.knowledgeBase.addEntry({
            type: 'skill_optimization',
            skillId,
            optimization,
            timestamp: new Date()
        });
    }
    
    public getEvolutionState(skillId: string): SkillEvolutionState | undefined {
        return this.evolutionStates.get(skillId);
    }
    
    public getEvolutionRecords(skillId: string): EvolutionRecord[] {
        return this.evolutionRecords.get(skillId) || [];
    }
    
    public setConfig(config: Partial<EvolutionConfig>): void {
        Object.assign(this.config, config);
        this.logger.info('Evolution config updated', this.config);
    }
    
    public dispose(): void {
        this.evolutionRecords.clear();
        this.evolutionStates.clear();
        SkillEvolution.instance = null;
    }
}
