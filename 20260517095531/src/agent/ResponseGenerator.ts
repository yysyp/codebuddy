import { Logger } from '../utils/Logger';
import { Intent } from './IntentRecognizer';

export interface ResponseOptions {
    intent: Intent;
    message: string;
    result?: unknown;
    skillUsed?: string;
}

export class ResponseGenerator {
    private readonly logger: Logger;
    private readonly responseTemplates: Map<string, string[]>;
    
    constructor() {
        this.logger = Logger.getInstance('ResponseGenerator');
        this.responseTemplates = this.initializeTemplates();
    }
    
    private initializeTemplates(): Map<string, string[]> {
        return new Map<string, string[]>([
            ['analyze', [
                'I analyzed the code and found the following issues: {result}',
                'After analysis: {result}',
                'Analysis complete. {result}'
            ]],
            ['refactor', [
                'I refactored the code. {result}',
                'Refactoring suggestions: {result}',
                'Here are the improvements: {result}'
            ]],
            ['document', [
                'Documentation generated: {result}',
                'Here is the documentation: {result}',
                'Documented successfully. {result}'
            ]],
            ['test', [
                'Tests generated: {result}',
                'Here are the unit tests: {result}',
                'Test cases created: {result}'
            ]],
            ['explain', [
                '{result}',
                'Here is the explanation: {result}',
                '{result}'
            ]],
            ['debug', [
                'Debug information: {result}',
                'Possible issues found: {result}',
                'Debug analysis: {result}'
            ]],
            ['optimize', [
                'Optimization suggestions: {result}',
                'Performance improvements: {result}',
                'Optimized code: {result}'
            ]],
            ['search', [
                'Found: {result}',
                'Search results: {result}',
                'Here is what I found: {result}'
            ]],
            ['create', [
                'Created successfully: {result}',
                'New file/component created: {result}',
                'Creation complete: {result}'
            ]],
            ['modify', [
                'Modified successfully: {result}',
                'Changes made: {result}',
                'Update complete: {result}'
            ]],
            ['delete', [
                'Deleted successfully: {result}',
                'Removed: {result}',
                'Deletion complete: {result}'
            ]],
            ['unknown', [
                'I can help with code analysis, refactoring, documentation, testing, and more.',
                'I understand you need help. Let me assist you.',
                'I\'m here to help with your development tasks.'
            ]]
        ]);
    }
    
    public async generate(options: ResponseOptions): Promise<string> {
        this.logger.debug('Generating response', {
            intent: options.intent.type,
            hasResult: !!options.result
        });
        
        const templates = this.responseTemplates.get(options.intent.type) || 
            this.responseTemplates.get('unknown')!;
        
        const template = templates[Math.floor(Math.random() * templates.length)];
        
        let resultString = '';
        if (options.result !== undefined) {
            if (typeof options.result === 'string') {
                resultString = options.result;
            } else if (typeof options.result === 'object') {
                resultString = JSON.stringify(options.result, null, 2);
            } else {
                resultString = String(options.result);
            }
        }
        
        let response = template.replace('{result}', resultString || 'Done.');
        
        if (options.skillUsed) {
            response += ` (via ${options.skillUsed})`;
        }
        
        return response;
    }
    
    public async generateError(error: Error, context?: string): Promise<string> {
        this.logger.error('Generating error response', { error, context });
        
        return `I encountered an error${context ? ` while ${context}` : ''}: ${error.message}. ` +
            'Please try again or provide more details.';
    }
    
    public async generateSuggestions(
        currentCode: string,
        intent: Intent
    ): Promise<string[]> {
        const suggestions: string[] = [];
        
        switch (intent.type) {
            case 'analyze':
                suggestions.push(
                    'Add error handling',
                    'Improve variable naming',
                    'Add type annotations',
                    'Consider performance implications'
                );
                break;
            case 'refactor':
                suggestions.push(
                    'Extract to smaller functions',
                    'Use design patterns',
                    'Reduce complexity',
                    'Improve naming conventions'
                );
                break;
            case 'document':
                suggestions.push(
                    'Add JSDoc comments',
                    'Document function parameters',
                    'Add usage examples',
                    'Describe return values'
                );
                break;
            case 'test':
                suggestions.push(
                    'Test edge cases',
                    'Add boundary tests',
                    'Mock external dependencies',
                    'Test error scenarios'
                );
                break;
        }
        
        return suggestions;
    }
    
    public addResponseTemplate(intent: string, templates: string[]): void {
        this.responseTemplates.set(intent, templates);
        this.logger.debug('Added response template', { intent, count: templates.length });
    }
    
    public getAvailableIntents(): string[] {
        return Array.from(this.responseTemplates.keys());
    }
}
