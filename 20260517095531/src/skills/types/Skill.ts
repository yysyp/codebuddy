import { Logger } from '../../utils/Logger';

export interface SkillContext {
    intent: {
        type: string;
        confidence: number;
        entities: Array<{ type: string; value: string; confidence: number }>;
        rawText: string;
    };
    message: string;
    context?: Record<string, unknown>;
}

export interface SkillResult {
    success: boolean;
    data?: unknown;
    message?: string;
    errors?: Array<{ field: string; code: string; message: string }>;
    metadata?: Record<string, unknown>;
}

export interface Skill {
    readonly id: string;
    readonly name: string;
    readonly description: string;
    readonly version: string;
    readonly tags: string[];
    readonly supportedIntents: string[];
    readonly supportedLanguages: string[];
    
    execute(context: SkillContext): Promise<SkillResult>;
    validate(): Promise<boolean>;
    getMetadata(): SkillMetadata;
}

export interface SkillMetadata {
    id: string;
    name: string;
    description: string;
    version: string;
    author?: string;
    tags: string[];
    supportedIntents: string[];
    supportedLanguages: string[];
    createdAt: Date;
    updatedAt: Date;
    usageCount: number;
    successRate: number;
}

export interface SkillPerformance {
    skillId: string;
    totalExecutions: number;
    successfulExecutions: number;
    failedExecutions: number;
    averageExecutionTime: number;
    lastExecuted: Date;
    successRate: number;
}

export interface SkillFeedback {
    skillId: string;
    userId: string;
    rating: number;
    comment?: string;
    timestamp: Date;
    helpful: boolean;
}

export type SkillState = 'loading' | 'ready' | 'executing' | 'error' | 'disabled';

export interface SkillStatistics {
    totalSkills: number;
    activeSkills: number;
    totalExecutions: number;
    averageSuccessRate: number;
    topPerformingSkills: SkillPerformance[];
}

export interface SkillConfiguration {
    enabled: boolean;
    priority: number;
    timeout: number;
    retries: number;
    cacheEnabled: boolean;
    cacheTTL: number;
}

export interface SkillFilter {
    tags?: string[];
    intents?: string[];
    languages?: string[];
    minSuccessRate?: number;
    enabledOnly?: boolean;
}

export interface SkillSortOptions {
    field: 'name' | 'successRate' | 'usageCount' | 'lastExecuted';
    direction: 'asc' | 'desc';
}

export interface SkillUpdate {
    version: string;
    changes: string[];
    breakingChanges?: string[];
    releaseDate: Date;
}

export interface SkillDependency {
    skillId: string;
    version: string;
    required: boolean;
}

export interface SkillCapability {
    name: string;
    description: string;
    parameters?: Record<string, unknown>;
}

export interface SkillTemplate {
    id: string;
    name: string;
    description: string;
    code: string;
    language: string;
}
