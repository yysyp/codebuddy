import * as vscode from 'vscode';
import { Logger } from '../utils/Logger';

export interface AgentConfigOptions {
    enabled: boolean;
    model: string;
    maxTokens: number;
    temperature: number;
    autoUpdate: boolean;
    learningEnabled: boolean;
    filterSensitiveData: boolean;
    allowedPatterns: string[];
    blockedPatterns: string[];
    learningInterval: number;
    maxKnowledgeEntries: number;
    securityLevel: 'high' | 'medium' | 'low';
}

export class AgentConfig {
    private static instance: AgentConfig | null = null;
    
    private readonly logger: Logger;
    private config: vscode.WorkspaceConfiguration;
    private readonly defaultConfig: AgentConfigOptions;
    
    private constructor() {
        this.logger = Logger.getInstance('AgentConfig');
        this.config = vscode.workspace.getConfiguration('developerAssistant');
        
        this.defaultConfig = {
            enabled: true,
            model: 'gpt-4',
            maxTokens: 4000,
            temperature: 0.7,
            autoUpdate: true,
            learningEnabled: true,
            filterSensitiveData: true,
            allowedPatterns: ['*.ts', '*.js', '*.json', '*.tsx', '*.jsx'],
            blockedPatterns: ['*.env', '*.key', '*password*', '*secret*', '*.pem', '*.crt'],
            learningInterval: 60000,
            maxKnowledgeEntries: 10000,
            securityLevel: 'high'
        };
    }
    
    public static getInstance(): AgentConfig {
        if (!AgentConfig.instance) {
            AgentConfig.instance = new AgentConfig();
        }
        return AgentConfig.instance;
    }
    
    public get<K extends keyof AgentConfigOptions>(key: K): AgentConfigOptions[K] {
        const value = this.config.get<AgentConfigOptions[K]>(key);
        return value !== undefined ? value : this.defaultConfig[key];
    }
    
    public set<K extends keyof AgentConfigOptions>(
        key: K,
        value: AgentConfigOptions[K]
    ): Promise<void> {
        return this.config.update(key, value, vscode.ConfigurationTarget.Workspace);
    }
    
    public getAll(): AgentConfigOptions {
        const result: AgentConfigOptions = { ...this.defaultConfig };
        
        for (const key of Object.keys(this.defaultConfig) as Array<keyof AgentConfigOptions>) {
            const value = this.config.get<AgentConfigOptions[typeof key]>(key);
            if (value !== undefined) {
                (result as Record<string, unknown>)[key] = value;
            }
        }
        
        return result;
    }
    
    public reset(): Promise<void> {
        for (const key of Object.keys(this.defaultConfig) as Array<keyof AgentConfigOptions>) {
            this.config.update(key, undefined, vscode.ConfigurationTarget.Workspace);
        }
        this.logger.info('Configuration reset to defaults');
        return Promise.resolve();
    }
    
    public isEnabled(): boolean {
        return this.get('enabled');
    }
    
    public isLearningEnabled(): boolean {
        return this.get('learningEnabled');
    }
    
    public isSensitiveDataFilteringEnabled(): boolean {
        return this.get('filterSensitiveData');
    }
    
    public getSecurityLevel(): 'high' | 'medium' | 'low' {
        return this.get('securityLevel');
    }
    
    public getSensitivePatterns(): string[] {
        const level = this.getSecurityLevel();
        
        const patterns: Record<string, string[]> = {
            high: [
                'password', 'passwd', 'secret', 'api_key', 'apikey', 'token',
                'credential', 'auth', 'private_key', 'privatekey', 'access_token',
                'refresh_token', 'bearer', 'authorization', 'jwt', 'session',
                'db_pass', 'database_password', 'connection_string', 'connectionstring'
            ],
            medium: [
                'password', 'secret', 'api_key', 'token', 'credential', 'key'
            ],
            low: [
                'password', 'secret', 'key'
            ]
        };
        
        return patterns[level];
    }
    
    public shouldProcessFile(fileName: string): boolean {
        const allowed = this.get('allowedPatterns');
        const blocked = this.get('blockedPatterns');
        
        for (const pattern of blocked) {
            if (this.matchPattern(fileName, pattern)) {
                return false;
            }
        }
        
        if (allowed.length === 0) {
            return true;
        }
        
        for (const pattern of allowed) {
            if (this.matchPattern(fileName, pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    private matchPattern(fileName: string, pattern: string): boolean {
        const regex = this.patternToRegex(pattern);
        return regex.test(fileName);
    }
    
    private patternToRegex(pattern: string): RegExp {
        const escaped = pattern
            .replace(/[.+^${}()|[\]\\]/g, '\\$&')
            .replace(/\*/g, '.*')
            .replace(/\?/g, '.');
        return new RegExp(`^${escaped}$`, 'i');
    }
    
    public onDidChangeConfiguration(
        callback: (config: AgentConfigOptions) => void
    ): vscode.Disposable {
        return vscode.workspace.onDidChangeConfiguration(event => {
            if (event.affectsConfiguration('developerAssistant')) {
                this.config = vscode.workspace.getConfiguration('developerAssistant');
                callback(this.getAll());
            }
        });
    }
    
    public dispose(): void {
        AgentConfig.instance = null;
    }
}
