import * as vscode from 'vscode';
import { Logger } from '../utils/Logger';

export interface ContextData {
    workspaceFolder: string | undefined;
    fileName: string | undefined;
    languageId: string | undefined;
    cursorPosition: vscode.Position | undefined;
    selection: vscode.Selection | undefined;
    visibleEditors: vscode.TextEditor[];
    openDocuments: vscode.TextDocument[];
}

export class AgentContext {
    private static instance: AgentContext | null = null;
    
    private readonly logger: Logger;
    private currentContext: ContextData;
    private contextHistory: ContextData[] = [];
    private readonly maxHistorySize: number = 50;
    
    private constructor() {
        this.logger = Logger.getInstance('AgentContext');
        this.currentContext = this.createEmptyContext();
    }
    
    public static getInstance(): AgentContext {
        if (!AgentContext.instance) {
            AgentContext.instance = new AgentContext();
        }
        return AgentContext.instance;
    }
    
    private createEmptyContext(): ContextData {
        return {
            workspaceFolder: undefined,
            fileName: undefined,
            languageId: undefined,
            cursorPosition: undefined,
            selection: undefined,
            visibleEditors: [],
            openDocuments: []
        };
    }
    
    public getCurrentContext(): ContextData {
        return { ...this.currentContext };
    }
    
    public updateEditorContext(editor: vscode.TextEditor): void {
        const document = editor.document;
        
        this.pushContext();
        
        this.currentContext = {
            workspaceFolder: vscode.workspace.workspaceFolders?.[0]?.uri.fsPath,
            fileName: document.fileName,
            languageId: document.languageId,
            cursorPosition: editor.selection.active,
            selection: editor.selection,
            visibleEditors: vscode.window.visibleTextEditors,
            openDocuments: vscode.workspace.textDocuments
        };
        
        this.logger.debug('Context updated', {
            fileName: this.currentContext.fileName,
            languageId: this.currentContext.languageId
        });
    }
    
    public updateWorkspaceContext(): void {
        this.pushContext();
        
        this.currentContext = {
            workspaceFolder: vscode.workspace.workspaceFolders?.[0]?.uri.fsPath,
            fileName: this.currentContext.fileName,
            languageId: this.currentContext.languageId,
            cursorPosition: this.currentContext.cursorPosition,
            selection: this.currentContext.selection,
            visibleEditors: vscode.window.visibleTextEditors,
            openDocuments: vscode.workspace.textDocuments
        };
        
        this.logger.debug('Workspace context updated', {
            workspaceFolder: this.currentContext.workspaceFolder
        });
    }
    
    private pushContext(): void {
        this.contextHistory.push({ ...this.currentContext });
        
        if (this.contextHistory.length > this.maxHistorySize) {
            this.contextHistory.shift();
        }
    }
    
    public getPreviousContext(steps: number = 1): ContextData | undefined {
        const index = this.contextHistory.length - steps;
        if (index >= 0 && index < this.contextHistory.length) {
            return this.contextHistory[index];
        }
        return undefined;
    }
    
    public getContextHistory(): ContextData[] {
        return [...this.contextHistory];
    }
    
    public getRelevantFiles(extension?: string): string[] {
        const files: string[] = [];
        
        if (vscode.workspace.workspaceFolders) {
            for (const folder of vscode.workspace.workspaceFolders) {
                const pattern = new vscode.RelativePattern(
                    folder,
                    extension ? `**/*${extension}` : '**/*'
                );
                
                vscode.workspace.findFiles(pattern, '**/node_modules/**', 100)
                    .then(uriFiles => {
                        files.push(...uriFiles.map(f => f.fsPath));
                    });
            }
        }
        
        return files;
    }
    
    public getCurrentFileContent(): string | undefined {
        const editor = vscode.window.activeTextEditor;
        if (editor) {
            return editor.document.getText();
        }
        return undefined;
    }
    
    public getSelectedText(): string | undefined {
        const editor = vscode.window.activeTextEditor;
        if (editor && !editor.selection.isEmpty) {
            return editor.document.getText(editor.selection);
        }
        return undefined;
    }
    
    public getLineContent(line: number): string | undefined {
        const editor = vscode.window.activeTextEditor;
        if (editor) {
            const document = editor.document;
            if (line >= 0 && line < document.lineCount) {
                return document.lineAt(line).text;
            }
        }
        return undefined;
    }
    
    public getSymbolAtCursor(): vscode.DocumentSymbol | undefined {
        const editor = vscode.window.activeTextEditor;
        if (!editor) {
            return undefined;
        }
        
        const position = editor.selection.active;
        
        return vscode.commands.executeCommand<vscode.DocumentSymbol[]>(
            'vscode.executeDocumentSymbolProvider',
            editor.document.uri
        ).then(symbols => {
            if (!symbols) {
                return undefined;
            }
            
            const findSymbol = (items: vscode.DocumentSymbol[]): vscode.DocumentSymbol | undefined => {
                for (const item of items) {
                    if (item.range.contains(position)) {
                        return item;
                    }
                    const childResult = findSymbol(item.children);
                    if (childResult) {
                        return childResult;
                    }
                }
                return undefined;
            };
            
            return findSymbol(symbols);
        }) as Promise<vscode.DocumentSymbol | undefined>;
    }
    
    public clearHistory(): void {
        this.contextHistory.length = 0;
        this.logger.debug('Context history cleared');
    }
    
    public dispose(): void {
        this.contextHistory.length = 0;
        AgentContext.instance = null;
    }
}
