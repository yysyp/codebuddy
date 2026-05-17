import { Logger } from '../utils/Logger';
import { Hook, HookContext, HookEvent, HookRegistration, HookState, HookStatistics } from './types/Hook';

export class HookRegistry {
    private static instance: HookRegistry | null = null;
    
    private readonly logger: Logger;
    private readonly hooks: Map<string, HookRegistration>;
    
    private constructor() {
        this.logger = Logger.getInstance('HookRegistry');
        this.hooks = new Map<string, HookRegistration>();
    }
    
    public static getInstance(): HookRegistry {
        if (!HookRegistry.instance) {
            HookRegistry.instance = new HookRegistry();
        }
        return HookRegistry.instance;
    }
    
    public async register(hook: Hook): Promise<void> {
        if (this.hooks.has(hook.id)) {
            this.logger.warn(`Hook ${hook.id} already registered`);
            return;
        }
        
        const registration: HookRegistration = {
            hook,
            state: hook.enabled ? 'enabled' : 'disabled',
            executionCount: 0,
            averageExecutionTime: 0
        };
        
        this.hooks.set(hook.id, registration);
        this.logger.info(`Registered hook: ${hook.id}`);
    }
    
    public async unregister(hookId: string): Promise<void> {
        if (!this.hooks.has(hookId)) {
            this.logger.warn(`Hook ${hookId} not found`);
            return;
        }
        
        this.hooks.delete(hookId);
        this.logger.info(`Unregistered hook: ${hookId}`);
    }
    
    public getHook(hookId: string): Hook | undefined {
        return this.hooks.get(hookId)?.hook;
    }
    
    public getAllHooks(): Hook[] {
        return Array.from(this.hooks.values())
            .filter(r => r.state === 'enabled')
            .map(r => r.hook);
    }
    
    public getHooksByEvent(event: HookEvent): Hook[] {
        return Array.from(this.hooks.values())
            .filter(r => r.hook.event === event && r.state === 'enabled')
            .map(r => r.hook);
    }
    
    public async setHookState(hookId: string, state: HookState): Promise<void> {
        const registration = this.hooks.get(hookId);
        if (!registration) {
            throw new Error(`Hook ${hookId} not found`);
        }
        
        registration.state = state;
        this.logger.info(`Hook ${hookId} state changed to ${state}`);
    }
    
    public getHookState(hookId: string): HookState | undefined {
        return this.hooks.get(hookId)?.state;
    }
    
    public recordExecution(hookId: string, executionTime: number): void {
        const registration = this.hooks.get(hookId);
        if (!registration) {
            return;
        }
        
        registration.executionCount++;
        registration.lastExecuted = new Date();
        registration.averageExecutionTime = 
            (registration.averageExecutionTime * (registration.executionCount - 1) + executionTime) / 
            registration.executionCount;
        registration.lastError = undefined;
    }
    
    public recordError(hookId: string, error: Error): void {
        const registration = this.hooks.get(hookId);
        if (!registration) {
            return;
        }
        
        registration.lastError = error;
        registration.state = 'error';
        this.logger.error(`Hook ${hookId} recorded error`, error);
    }
    
    public getStatistics(): HookStatistics {
        const registrations = Array.from(this.hooks.values());
        
        let totalExecutions = 0;
        let failedExecutions = 0;
        let totalTime = 0;
        
        for (const reg of registrations) {
            totalExecutions += reg.executionCount;
            if (reg.lastError) {
                failedExecutions++;
            }
            totalTime += reg.averageExecutionTime * reg.executionCount;
        }
        
        return {
            totalHooks: registrations.length,
            activeHooks: registrations.filter(r => r.state === 'enabled').length,
            totalExecutions,
            failedExecutions,
            averageExecutionTime: totalExecutions > 0 ? totalTime / totalExecutions : 0
        };
    }
    
    public clear(): void {
        this.hooks.clear();
        this.logger.info('Hook registry cleared');
    }
    
    public dispose(): void {
        this.hooks.clear();
        HookRegistry.instance = null;
    }
}
