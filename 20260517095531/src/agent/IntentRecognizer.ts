import { Logger } from '../utils/Logger';

export interface Intent {
    type: IntentType;
    confidence: number;
    entities: IntentEntity[];
    rawText: string;
}

export type IntentType = 
    | 'analyze'
    | 'refactor'
    | 'document'
    | 'test'
    | 'explain'
    | 'debug'
    | 'optimize'
    | 'search'
    | 'create'
    | 'modify'
    | 'delete'
    | 'unknown';

export interface IntentEntity {
    type: EntityType;
    value: string;
    confidence: number;
}

export type EntityType = 
    | 'file'
    | 'function'
    | 'class'
    | 'variable'
    | 'module'
    | 'project'
    | 'language'
    | 'framework'
    | 'tool'
    | 'action';

export class IntentRecognizer {
    private readonly logger: Logger;
    private readonly intentPatterns: Map<IntentType, RegExp[]>;
    private readonly entityPatterns: Map<EntityType, RegExp[]>;
    
    constructor() {
        this.logger = Logger.getInstance('IntentRecognizer');
        this.intentPatterns = this.initializeIntentPatterns();
        this.entityPatterns = this.initializeEntityPatterns();
    }
    
    private initializeIntentPatterns(): Map<IntentType, RegExp[]> {
        return new Map<IntentType, RegExp[]>([
            ['analyze', [
                /analyze|analysis|review|inspect|examine|check/i,
                /what('s| is) wrong|problem|issue|error/i,
                /find.*bug|detect.*issue/i
            ]],
            ['refactor', [
                /refactor|restructure|reorganize|clean/i,
                /improve|optimize.*code|enhance/i,
                /extract.*method|rename.*variable/i
            ]],
            ['document', [
                /document|doc|comment|explain.*code/i,
                /generate.*doc|add.*documentation/i,
                /what.*does.*this.*do|how.*work/i
            ]],
            ['test', [
                /test|testing|unit.*test|spec/i,
                /generate.*test|write.*test|create.*test/i,
                /test.*case|coverage/i
            ]],
            ['explain', [
                /explain|describe|clarify|understand/i,
                /what.*is|how.*does|tell.*me.*about/i,
                /why.*is|reason.*for/i
            ]],
            ['debug', [
                /debug|debugging|fix.*error|troubleshoot/i,
                /error.*in|exception|stack.*trace/i,
                /not.*working|broken|failed/i
            ]],
            ['optimize', [
                /optimize|performance|speed.*up|faster/i,
                /improve.*performance|reduce.*time/i,
                /make.*efficient|bottleneck/i
            ]],
            ['search', [
                /search|find|lookup|locate/i,
                /where.*is|find.*file|search.*for/i,
                /look.*up|get.*information/i
            ]],
            ['create', [
                /create|new|generate|build|add/i,
                /make.*new|generate.*new/i,
                /initialize|start.*new/i
            ]],
            ['modify', [
                /modify|change|update|edit/i,
                /alter|adjust|revise/i,
                /make.*change|update.*code/i
            ]],
            ['delete', [
                /delete|remove|clean.*up/i,
                /get.*rid.*of|eliminate/i,
                /uninstall|drop/i
            ]]
        ]);
    }
    
    private initializeEntityPatterns(): Map<EntityType, RegExp[]> {
        return new Map<EntityType, RegExp[]>([
            ['file', [
                /file\s+[\w./\\]+\.\w+/i,
                /[\w./\\]+\.\w+/,
                /\.(ts|js|tsx|jsx|json|md|txt|xml|html|css)$/i
            ]],
            ['function', [
                /function\s+\w+/i,
                /(?:function|method)\s+["']?(\w+)["']?/i,
                /(?:def|fn)\s+\w+/i
            ]],
            ['class', [
                /class\s+\w+/i,
                /class\s+["']?(\w+)["']?/i
            ]],
            ['variable', [
                /variable\s+\w+/i,
                /(?:const|let|var)\s+\w+/i
            ]],
            ['module', [
                /module\s+\w+/i,
                /(?:import|require).*from/i
            ]],
            ['project', [
                /project\s+\w+/i,
                /(?:workspace|folder|directory)/i
            ]],
            ['language', [
                /(?:typescript|javascript|python|java|csharp|go|rust|ruby)/i,
                /(?:ts|js|py|java|c#|go|rs|rb)/i
            ]],
            ['framework', [
                /(?:react|vue|angular|express|spring|django|flask)/i,
                /(?:node|electron|reactnative)/i
            ]],
            ['tool', [
                /(?:git|npm|yarn|docker|kubernetes|jenkins)/i,
                /(?:eslint|prettier|webpack|vite)/i
            ]],
            ['action', [
                /(?:add|remove|create|update|delete|get|set)\w*/i
            ]]
        ]);
    }
    
    public async recognize(text: string): Promise<Intent> {
        this.logger.debug('Recognizing intent from text', { length: text.length });
        
        const intent = this.recognizeIntent(text);
        const entities = this.recognizeEntities(text);
        
        return {
            type: intent.type,
            confidence: intent.confidence,
            entities,
            rawText: text
        };
    }
    
    private recognizeIntent(text: string): { type: IntentType; confidence: number } {
        let bestMatch: IntentType = 'unknown';
        let highestConfidence: number = 0;
        
        for (const [intentType, patterns] of this.intentPatterns.entries()) {
            let matchCount = 0;
            
            for (const pattern of patterns) {
                if (pattern.test(text)) {
                    matchCount++;
                }
            }
            
            const confidence = patterns.length > 0 
                ? matchCount / patterns.length 
                : 0;
            
            if (confidence > highestConfidence) {
                highestConfidence = confidence;
                bestMatch = intentType;
            }
        }
        
        return {
            type: bestMatch,
            confidence: Math.min(highestConfidence, 1.0)
        };
    }
    
    private recognizeEntities(text: string): IntentEntity[] {
        const entities: IntentEntity[] = [];
        
        for (const [entityType, patterns] of this.entityPatterns.entries()) {
            for (const pattern of patterns) {
                const matches = text.match(pattern);
                if (matches) {
                    entities.push({
                        type: entityType,
                        value: matches[1] || matches[0],
                        confidence: 0.8
                    });
                    break;
                }
            }
        }
        
        return entities;
    }
    
    public addIntentPattern(type: IntentType, pattern: RegExp): void {
        const patterns = this.intentPatterns.get(type);
        if (patterns) {
            patterns.push(pattern);
        } else {
            this.intentPatterns.set(type, [pattern]);
        }
        this.logger.debug('Added intent pattern', { type, pattern: pattern.source });
    }
    
    public addEntityPattern(type: EntityType, pattern: RegExp): void {
        const patterns = this.entityPatterns.get(type);
        if (patterns) {
            patterns.push(pattern);
        } else {
            this.entityPatterns.set(type, [pattern]);
        }
        this.logger.debug('Added entity pattern', { type, pattern: pattern.source });
    }
    
    public clearCustomPatterns(): void {
        this.intentPatterns.clear();
        this.entityPatterns.clear();
        this.intentPatterns.set('unknown', []);
        this.entityPatterns.set('action', []);
        this.logger.info('Custom patterns cleared');
    }
}
