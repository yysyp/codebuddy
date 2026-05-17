import * as vscode from 'vscode';
import { Logger } from '../utils/Logger';
import { AgentConfig } from './AgentConfig';
import { AgentContext } from './AgentContext';
import { IntentRecognizer } from './IntentRecognizer';
import { ResponseGenerator } from './ResponseGenerator';
import { SkillRegistry } from '../skills/SkillRegistry';
import { HookManager } from '../hooks/HookManager';
import { SensitiveDataFilter } from '../security/SensitiveDataFilter';
import { EvolutionEngine } from '../learning/EvolutionEngine';

export interface AgentMessage {
    role: 'user' | 'assistant' | 'system';
    content: string;
    timestamp: Date;
    context?: Record<string, unknown>;
}

export interface AgentResponse {
    success: boolean;
    message: string;
    data?: unknown;
    skillUsed?: string;
    timestamp: Date;
}

export class AgentCore {
    private static instance: AgentCore | null = null;
    
    private readonly logger: Logger;
    private readonly config: AgentConfig;
    private readonly context: AgentContext;
    private readonly intentRecognizer: IntentRecognizer;
    private readonly responseGenerator: ResponseGenerator;
    private readonly skillRegistry: SkillRegistry;
    private readonly hookManager: HookManager;
    private readonly sensitiveDataFilter: SensitiveDataFilter;
    private readonly evolutionEngine: EvolutionEngine;
    
    private readonly messageHistory: AgentMessage[] = [];
    private isInitialized: boolean = false;
    private statusBarItem: vscode.StatusBarItem | null = null;
    
    private constructor() {
        this.logger = Logger.getInstance('AgentCore');
        this.config = AgentConfig.getInstance();
        this.context = AgentContext.getInstance();
        this.intentRecognizer = new IntentRecognizer();
        this.responseGenerator = new ResponseGenerator();
        this.skillRegistry = SkillRegistry.getInstance();
        this.hookManager = HookManager.getInstance();
        this.sensitiveDataFilter = SensitiveDataFilter.getInstance();
        this.evolutionEngine = EvolutionEngine.getInstance();
    }
    
    public static getInstance(): AgentCore {
        if (!AgentCore.instance) {
            AgentCore.instance = new AgentCore();
        }
        return AgentCore.instance;
    }
    
    public async initialize(): Promise<void> {
        if (this.isInitialized) {
            this.logger.warn('AgentCore already initialized');
            return;
        }
        
        try {
            this.logger.info('Initializing AgentCore...');
            
            await this.skillRegistry.initialize();
            await this.hookManager.initialize();
            await this.evolutionEngine.initialize();
            
            this.createStatusBar();
            this.registerCommands();
            this.registerEventHandlers();
            
            this.isInitialized = true;
            this.logger.info('AgentCore initialized successfully');
            
            this.updateStatus('ready');
        } catch (error) {
            this.logger.error('Failed to initialize AgentCore', error);
            throw error;
        }
    }
    
    private createStatusBar(): void {
        this.statusBarItem = vscode.window.createStatusBarItem(
            vscode.StatusBarAlignment.Left,
            100
        );
        this.statusBarItem.text = '$(robot) Developer Assistant';
        this.statusBarItem.tooltip = 'Developer Assistant Agent';
        this.statusBarItem.command = 'developerAssistant.start';
        this.statusBarItem.show();
    }
    
    private registerCommands(): void {
        const commands = [
            { command: 'developerAssistant.start', handler: this.start.bind(this) },
            { command: 'developerAssistant.configure', handler: this.configure.bind(this) },
            { command: 'developerAssistant.analyze', handler: this.analyzeCurrentFile.bind(this) },
            { command: 'developerAssistant.refactor', handler: this.refactorSelection.bind(this) },
            { command: 'developerAssistant.generateDoc', handler: this.generateDocumentation.bind(this) },
            { command: 'developerAssistant.generateTests', handler: this.generateTests.bind(this) },
            { command: 'developerAssistant.learn', handler: this.enableLearning.bind(this) },
            { command: 'developerAssistant.skillList', handler: this.listSkills.bind(this) },
            { command: 'developerAssistant.hookList', handler: this.listHooks.bind(this) },
            { command: 'developerAssistant.optimize', handler: this.optimizeSkills.bind(this) }
        ];
        
        commands.forEach(({ command, handler }) => {
            vscode.commands.registerCommand(command, handler);
        });
    }
    
    private registerEventHandlers(): void {
        vscode.workspace.onWillSaveTextDocument(this.onWillSaveDocument.bind(this));
        vscode.workspace.onDidSaveTextDocument(this.onDidSaveDocument.bind(this));
        vscode.workspace.onDidChangeTextDocument(this.onDidChangeTextDocument.bind(this));
        vscode.window.onDidChangeActiveTextEditor(this.onDidChangeActiveEditor.bind(this));
    }
    
    public async processMessage(userMessage: string): Promise<AgentResponse> {
        try {
            this.logger.info('Processing user message', { length: userMessage.length });
            
            const filteredMessage = this.sensitiveDataFilter.filter(userMessage);
            this.logger.debug('Message filtered for sensitive data');
            
            const intent = await this.intentRecognizer.recognize(filteredMessage);
            this.logger.info('Intent recognized', { intent });
            
            const relevantSkills = await this.skillRegistry.findSkills(intent);
            this.logger.info('Skills found', { count: relevantSkills.length });
            
            let result: unknown;
            let skillUsed: string | undefined;
            
            for (const skill of relevantSkills) {
                try {
                    const skillResult = await skill.execute({
                        intent,
                        message: filteredMessage,
                        context: this.context.getCurrentContext()
                    });
                    
                    if (skillResult.success) {
                        result = skillResult.data;
                        skillUsed = skill.id;
                        break;
                    }
                } catch (error) {
                    this.logger.warn(`Skill ${skill.id} failed`, error);
                }
            }
            
            const response = await this.responseGenerator.generate({
                intent,
                message: filteredMessage,
                result,
                skillUsed
            });
            
            this.recordInteraction(userMessage, response);
            
            await this.evolutionEngine.recordAndLearn(userMessage, intent, result);
            
            return {
                success: true,
                message: response,
                data: result,
                skillUsed,
                timestamp: new Date()
            };
        } catch (error) {
            this.logger.error('Failed to process message', error);
            return {
                success: false,
                message: `Error: ${(error as Error).message}`,
                timestamp: new Date()
            };
        }
    }
    
    private recordInteraction(userMessage: string, response: string): void {
        this.messageHistory.push({
            role: 'user',
            content: userMessage,
            timestamp: new Date()
        });
        this.messageHistory.push({
            role: 'assistant',
            content: response,
            timestamp: new Date()
        });
        
        if (this.messageHistory.length > 100) {
            this.messageHistory.splice(0, 50);
        }
    }
    
    private async start(): Promise<void> {
        const message = await vscode.window.showInputBox({
            prompt: 'Enter your request for Developer Assistant',
            placeHolder: 'e.g., Analyze this code, refactor function X, generate tests'
        });
        
        if (message) {
            const response = await this.processMessage(message);
            vscode.window.showInformationMessage(response.message);
        }
    }
    
    private async configure(): Promise<void> {
        await vscode.commands.executeCommand('workbench.action.openSettings', 'developerAssistant');
    }
    
    private async analyzeCurrentFile(): Promise<void> {
        const editor = vscode.window.activeTextEditor;
        if (!editor) {
            vscode.window.showWarningMessage('No active editor');
            return;
        }
        
        const document = editor.document;
        const message = `Analyze the file: ${document.fileName}`;
        const response = await this.processMessage(message);
        
        vscode.window.showInformationMessage(response.message);
    }
    
    private async refactorSelection(): Promise<void> {
        const editor = vscode.window.activeTextEditor;
        if (!editor || !editor.selection) {
            vscode.window.showWarningMessage('No selection found');
            return;
        }
        
        const selectedText = editor.selection.active;
        const message = `Refactor the selected code`;
        const response = await this.processMessage(message);
        
        vscode.window.showInformationMessage(response.message);
    }
    
    private async generateDocumentation(): Promise<void> {
        const editor = vscode.window.activeTextEditor;
        if (!editor) {
            vscode.window.showWarningMessage('No active editor');
            return;
        }
        
        const message = `Generate documentation for the current file`;
        const response = await this.processMessage(message);
        
        vscode.window.showInformationMessage(response.message);
    }
    
    private async generateTests(): Promise<void> {
        const editor = vscode.window.activeTextEditor;
        if (!editor) {
            vscode.window.showWarningMessage('No active editor');
            return;
        }
        
        const message = `Generate unit tests for the current file`;
        const response = await this.processMessage(message);
        
        vscode.window.showInformationMessage(response.message);
    }
    
    private async enableLearning(): Promise<void> {
        this.config.set('learningEnabled', true);
        vscode.window.showInformationMessage('Learning mode enabled');
    }
    
    private async listSkills(): Promise<void> {
        const skills = this.skillRegistry.getAllSkills();
        const skillList = skills.map(s => `${s.name} (${s.id}) - ${s.description}`).join('\n');
        
        vscode.window.showInformationMessage(`Available Skills:\n${skillList}`);
    }
    
    private async listHooks(): Promise<void> {
        const hooks = this.hookManager.getAllHooks();
        const hookList = hooks.map(h => `${h.name} - ${h.description}`).join('\n');
        
        vscode.window.showInformationMessage(`Active Hooks:\n${hookList}`);
    }
    
    private async optimizeSkills(): Promise<void> {
        await this.evolutionEngine.optimizeAllSkills();
        vscode.window.showInformationMessage('Skills optimized successfully');
    }
    
    private async onWillSaveDocument(event: vscode.TextDocumentWillSaveEvent): Promise<void> {
        await this.hookManager.executePreSaveHooks(event);
    }
    
    private async onDidSaveDocument(document: vscode.TextDocument): Promise<void> {
        await this.hookManager.executePostSaveHooks(document);
    }
    
    private async onDidChangeTextDocument(event: vscode.TextDocumentChangeEvent): Promise<void> {
        await this.hookManager.executeChangeHooks(event);
    }
    
    private async onDidChangeActiveEditor(editor: vscode.TextEditor | undefined): Promise<void> {
        if (editor) {
            this.context.updateEditorContext(editor);
        }
    }
    
    private updateStatus(status: string): void {
        if (this.statusBarItem) {
            this.statusBarItem.text = `$(robot) ${status}`;
        }
    }
    
    public getMessageHistory(): AgentMessage[] {
        return [...this.messageHistory];
    }
    
    public clearHistory(): void {
        this.messageHistory.length = 0;
    }
    
    public dispose(): void {
        if (this.statusBarItem) {
            this.statusBarItem.dispose();
        }
        this.skillRegistry.dispose();
        this.hookManager.dispose();
        this.evolutionEngine.dispose();
        this.isInitialized = false;
        AgentCore.instance = null;
    }
}
