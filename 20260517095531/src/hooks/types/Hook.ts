export interface HookContext {
    document?: {
        uri: string;
        fileName: string;
        languageId: string;
        content: string;
    };
    editor?: {
        uri: string;
        fileName: string;
        selection?: {
            start: { line: number; character: number };
            end: { line: number; character: number };
        };
    };
    workspace?: {
        rootPath: string;
        name: string;
    };
    timestamp: Date;
    metadata?: Record<string, unknown>;
}

export interface Hook {
    readonly id: string;
    readonly name: string;
    readonly description: string;
    readonly event: HookEvent;
    readonly priority: number;
    readonly enabled: boolean;
    
    before?(context: HookContext): Promise<void> | void;
    after?(context: HookContext, result?: HookResult): Promise<void> | void;
    onError?(error: Error, context: HookContext): Promise<void> | void;
}

export type HookEvent = 
    | 'onStartup'
    | 'onShutdown'
    | 'onOpenDocument'
    | 'onSaveDocument'
    | 'onCloseDocument'
    | 'onChangeDocument'
    | 'onChangeSelection'
    | 'onChangeActiveEditor'
    | 'onChangeWindowState'
    | 'onCommand'
    | 'onTaskStart'
    | 'onTaskEnd'
    | 'onDebugStart'
    | 'onDebugStop'
    | 'onExtensionInstall'
    | 'onExtensionUninstall';

export interface HookResult {
    success: boolean;
    data?: unknown;
    error?: string;
    executionTime: number;
}

export interface HookRegistration {
    hook: Hook;
    state: HookState;
    executionCount: number;
    lastExecuted?: Date;
    lastError?: Error;
    averageExecutionTime: number;
}

export type HookState = 'registered' | 'enabled' | 'disabled' | 'error';

export interface HookFilter {
    event?: HookEvent;
    enabled?: boolean;
    minPriority?: number;
    maxPriority?: number;
}

export interface HookStatistics {
    totalHooks: number;
    activeHooks: number;
    totalExecutions: number;
    failedExecutions: number;
    averageExecutionTime: number;
}

export interface HookConfig {
    timeout: number;
    retryCount: number;
    continueOnError: boolean;
    logExecutions: boolean;
}

export interface HookEventPayload {
    event: HookEvent;
    context: HookContext;
    timestamp: Date;
}

export type HookMiddleware = (
    context: HookContext,
    next: () => Promise<void>
) => Promise<void>;

export interface HookChain {
    hooks: Hook[];
    execute(context: HookContext): Promise<void>;
    addMiddleware(middleware: HookMiddleware): void;
}
