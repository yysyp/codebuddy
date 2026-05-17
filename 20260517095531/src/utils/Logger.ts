import * as vscode from 'vscode';

export enum LogLevel {
    DEBUG = 0,
    INFO = 1,
    WARN = 2,
    ERROR = 3
}

export class Logger {
    private static instance: Logger | null = null;
    private static logLevel: LogLevel = LogLevel.INFO;
    private static outputChannel: vscode.OutputChannel | null = null;
    
    private readonly name: string;
    private readonly context: string;
    
    private constructor(name: string) {
        this.name = name;
        this.context = `[${name}]`;
    }
    
    public static getInstance(name: string): Logger {
        if (!Logger.instance) {
            Logger.instance = new Logger(name);
        }
        return Logger.instance;
    }
    
    public static setLogLevel(level: LogLevel): void {
        Logger.logLevel = level;
    }
    
    public static getOutputChannel(): vscode.OutputChannel {
        if (!Logger.outputChannel) {
            Logger.outputChannel = vscode.window.createOutputChannel('Developer Assistant');
        }
        return Logger.outputChannel;
    }
    
    public debug(message: string, data?: unknown): void {
        if (Logger.logLevel <= LogLevel.DEBUG) {
            this.log('DEBUG', message, data);
        }
    }
    
    public info(message: string, data?: unknown): void {
        if (Logger.logLevel <= LogLevel.INFO) {
            this.log('INFO', message, data);
        }
    }
    
    public warn(message: string, data?: unknown): void {
        if (Logger.logLevel <= LogLevel.WARN) {
            this.log('WARN', message, data);
        }
    }
    
    public error(message: string, error?: unknown): void {
        if (Logger.logLevel <= LogLevel.ERROR) {
            let errorMessage = 'Unknown error';
            if (error instanceof Error) {
                errorMessage = error.message;
            } else if (typeof error === 'string') {
                errorMessage = error;
            }
            
            this.log('ERROR', `${message}: ${errorMessage}`, error);
        }
    }
    
    private log(level: string, message: string, data?: unknown): void {
        const timestamp = new Date().toISOString();
        const logMessage = `${timestamp} ${level} ${this.context} ${message}`;
        
        const output = data 
            ? `${logMessage}\n${JSON.stringify(data, null, 2)}`
            : logMessage;
        
        Logger.getOutputChannel().appendLine(output);
        
        console.log(output);
    }
    
    public static dispose(): void {
        if (Logger.outputChannel) {
            Logger.outputChannel.dispose();
            Logger.outputChannel = null;
        }
        Logger.instance = null;
    }
}
