import * as vscode from 'vscode';
import { Logger } from '../utils/Logger';
import { Hook, HookContext, HookEvent, HookResult, HookRegistration, HookState } from './types/Hook';
import { HookRegistry } from './HookRegistry';

export class HookManager {
    private static instance: HookManager | null = null;
    
    private readonly logger: Logger;
    private readonly registry: HookRegistry;
    private readonly config: HookConfig;
    private isInitialized: boolean = false;
    private disposables: vscode.Disposable[] = [];
    
    private constructor() {
        this.logger = Logger.getInstance('HookManager');
        this.registry = HookRegistry.getInstance();
        
        this.config = {
            timeout: 5000,
            retryCount: 3,
            continueOnError: true,
            logExecutions: true
        };
    }
    
    public static getInstance(): HookManager {
        if (!HookManager.instance) {
            HookManager.instance = new HookManager();
        }
        return HookManager.instance;
    }
    
    public async initialize(): Promise<void> {
        if (this.isInitialized) {
            this.logger.warn('HookManager already initialized');
            return;
        }
        
        this.logger.info('Initializing HookManager...');
        
        this.registerEventHandlers();
        
        this.isInitialized = true;
        this.logger.info('HookManager initialized');
    }
    
    private registerEventHandlers(): void {
        this.disposables.push(
            vscode.workspace.onDidOpenTextDocument(this.onOpenDocument.bind(this)),
            vscode.workspace.onDidSaveTextDocument(this.onSaveDocument.bind(this)),
            vscode.workspace.onDidCloseTextDocument(this.onCloseDocument.bind(this)),
            vscode.workspace.onDidChangeTextDocument(this.onChangeDocument.bind(this)),
            vscode.window.onDidChangeTextEditorSelection(this.onChangeSelection.bind(this)),
            vscode.window.onDidChangeActiveTextEditor(this.onChangeActiveEditor.bind(this)),
            vscode.window.onDidChangeWindowState(this.onChangeWindowState.bind(this)),
            vscode.commands.registerCommand('developerAssistant.hookTrigger', this.triggerHook.bind(this))
        );
        
        this.logger.debug('Registered event handlers');
    }
    
    public async registerHook(hook: Hook): Promise<void> {
        await this.registry.register(hook);
        this.logger.info(`Registered hook: ${hook.id} (${hook.name})`);
    }
    
    public async unregisterHook(hookId: string): Promise<void> {
        await this.registry.unregister(hookId);
        this.logger.info(`Unregistered hook: ${hookId}`);
    }
    
    public async enableHook(hookId: string): Promise<void> {
        await this.registry.setHookState(hookId, 'enabled');
    }
    
    public async disableHook(hookId: string): Promise<void> {
        await this.registry.setHookState(hookId, 'disabled');
    }
    
    public getAllHooks(): Hook[] {
        return this.registry.getAllHooks();
    }
    
    public getHooksByEvent(event: HookEvent): Hook[] {
        return this.registry.getHooksByEvent(event);
    }
    
    public async executePreSaveHooks(event: vscode.TextDocumentWillSaveEvent): Promise<void> {
        const context = this.createHookContext(event.document);
        
        await this.executeHooks('onSaveDocument', context, async (hook) => {
            if (hook.before) {
                await hook.before(context);
            }
        });
    }
    
    public async executePostSaveHooks(document: vscode.TextDocument): Promise<void> {
        const context = this.createHookContext(document);
        
        await this.executeHooks('onSaveDocument', context, async (hook) => {
            if (hook.after) {
                await hook.after(context);
            }
        });
    }
    
    public async executeChangeHooks(event: vscode.TextDocumentChangeEvent): Promise<void> {
        const context = this.createHookContext(event.document);
        
        await this.executeHooks('onChangeDocument', context, async (hook) => {
            if (hook.before) {
                await hook.before(context);
            }
        });
    }
    
    private async onOpenDocument(document: vscode.TextDocument): Promise<void> {
        const context = this.createHookContext(document);
        await this.executeHooks('onOpenDocument', context);
    }
    
    private async onSaveDocument(event: vscode.TextDocumentWillSaveEvent): Promise<void> {
        const context = this.createHookContext(event.document);
        await this.executeHooks('onSaveDocument', context);
    }
    
    private async onCloseDocument(document: vscode.TextDocument): Promise<void> {
        const context = this.createHookContext(document);
        await this.executeHooks('onCloseDocument', context);
    }
    
    private async onChangeDocument(event: vscode.TextDocumentChangeEvent): Promise<void> {
        const context = this.createHookContext(event.document);
        await this.executeHooks('onChangeDocument', context);
    }
    
    private async onChangeSelection(event: vscode.TextEditorSelectionChangeEvent): Promise<void> {
        const context = this.createSelectionContext(event);
        await this.executeHooks('onChangeSelection', context);
    }
    
    private async onChangeActiveEditor(event: vscode.TextEditor | undefined): Promise<void> {
        if (event) {
            const context = this.createEditorContext(event);
            await this.executeHooks('onChangeActiveEditor', context);
        }
    }
    
    private async onChangeWindowState(event: vscode.WindowState): Promise<void> {
        const context: HookContext = {
            timestamp: new Date(),
            metadata: {
                focused: event.focused
            }
        };
        await this.executeHooks('onChangeWindowState', context);
    }
    
    private createHookContext(document: vscode.TextDocument): HookContext {
        return {
            document: {
                uri: document.uri.toString(),
                fileName: document.fileName,
                languageId: document.languageId,
                content: document.getText()
            },
            workspace: vscode.workspace.workspaceFolders?.[0] ? {
                rootPath: vscode.workspace.workspaceFolders[0].uri.fsPath,
                name: vscode.workspace.workspaceFolders[0].name
            } : undefined,
            timestamp: new Date()
        };
    }
    
    private createEditorContext(editor: vscode.TextEditor): HookContext {
        const selection = editor.selection;
        
        return {
            editor: {
                uri: editor.document.uri.toString(),
                fileName: editor.document.fileName,
                selection: selection ? {
                    start: { line: selection.start.line, character: selection.start.character },
                    end: { line: selection.end.line, character: selection.end.character }
                } : undefined
            },
            document: {
                uri: editor.document.uri.toString(),
                fileName: editor.document.fileName,
                languageId: editor.document.languageId,
                content: editor.document.getText()
            },
            workspace: vscode.workspace.workspaceFolders?.[0] ? {
                rootPath: vscode.workspace.workspaceFolders[0].uri.fsPath,
                name: vscode.workspace.workspaceFolders[0].name
            } : undefined,
            timestamp: new Date()
        };
    }
    
    private createSelectionContext(event: vscode.TextEditorSelectionChangeEvent): HookContext {
        const selection = event.selection;
        
        return {
            editor: {
                uri: event.textEditor.document.uri.toString(),
                fileName: event.textEditor.document.fileName,
                selection: {
                    start: { line: selection.start.line, character: selection.start.character },
                    end: { line: selection.end.line, character: selection.end.character }
                }
            },
            document: {
                uri: event.textEditor.document.uri.toString(),
                fileName: event.textEditor.document.fileName,
                languageId: event.textEditor.document.languageId,
                content: event.textEditor.document.getText()
            },
            timestamp: new Date()
        };
    }
    
    private async executeHooks(
        event: HookEvent,
        context: HookContext,
        callback?: (hook: Hook) => Promise<void>
    ): Promise<void> {
        const hooks = this.registry.getHooksByEvent(event);
        
        if (hooks.length === 0) {
            return;
        }
        
        hooks.sort((a, b) => b.priority - a.priority);
        
        for (const hook of hooks) {
            if (!hook.enabled) {
                continue;
            }
            
            const startTime = Date.now();
            
            try {
                if (callback) {
                    await this.executeWithTimeout(callback(hook), hook.id);
                }
                
                this.registry.recordExecution(hook.id, Date.now() - startTime);
                
                if (this.config.logExecutions) {
                    this.logger.debug(`Hook ${hook.id} executed`, {
                        event,
                        duration: Date.now() - startTime
                    });
                }
            } catch (error) {
                this.logger.error(`Hook ${hook.id} execution failed`, error);
                
                this.registry.recordError(hook.id, error as Error);
                
                if (hook.onError) {
                    await hook.onError(error as Error, context);
                }
                
                if (!this.config.continueOnError) {
                    break;
                }
            }
        }
    }
    
    private async executeWithTimeout<T>(promise: Promise<T>, hookId: string): Promise<T> {
        return Promise.race([
            promise,
            new Promise<T>((_, reject) =>
                setTimeout(() => reject(new Error(`Hook ${hookId} timeout`)), this.config.timeout)
            )
        ]);
    }
    
    private async triggerHook(hookId: string): Promise<void> {
        const hook = this.registry.getHook(hookId);
        if (hook) {
            const context: HookContext = {
                timestamp: new Date()
            };
            await this.executeHooks(hook.event, context);
        }
    }
    
    public getStatistics() {
        return this.registry.getStatistics();
    }
    
    public setConfig(config: Partial<HookConfig>): void {
        Object.assign(this.config, config);
    }
    
    public dispose(): void {
        for (const disposable of this.disposables) {
            disposable.dispose();
        }
        this.disposables = [];
        this.isInitialized = false;
        HookManager.instance = null;
        this.logger.info('HookManager disposed');
    }
}
