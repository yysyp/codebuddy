import { Logger } from '../utils/Logger';
import { Skill } from './types/Skill';

export interface ValidationResult {
    valid: boolean;
    errors: ValidationError[];
    warnings: ValidationWarning[];
}

export interface ValidationError {
    field: string;
    code: string;
    message: string;
}

export interface ValidationWarning {
    field: string;
    code: string;
    message: string;
}

export class SkillValidator {
    private readonly logger: Logger;
    private readonly validators: Map<string, (skill: Skill) => ValidationResult>;
    
    constructor() {
        this.logger = Logger.getInstance('SkillValidator');
        this.validators = new Map<string, (skill: Skill) => ValidationResult>();
        
        this.registerDefaultValidators();
    }
    
    private registerDefaultValidators(): void {
        this.validators.set('basic', this.validateBasicStructure.bind(this));
        this.validators.set('interface', this.validateInterface.bind(this));
        this.validators.set('metadata', this.validateMetadata.bind(this));
        this.validators.set('dependencies', this.validateDependencies.bind(this));
    }
    
    public async validate(skill: Skill): Promise<boolean> {
        const result = await this.validateAll(skill);
        return result.valid;
    }
    
    public async validateAll(skill: Skill): Promise<ValidationResult> {
        const allErrors: ValidationError[] = [];
        const allWarnings: ValidationWarning[] = [];
        
        for (const [, validator] of this.validators.entries()) {
            const result = validator(skill);
            allErrors.push(...result.errors);
            allWarnings.push(...result.warnings);
        }
        
        const valid = allErrors.length === 0;
        
        if (!valid) {
            this.logger.warn(`Skill ${skill.id} validation failed`, { errors: allErrors });
        }
        
        return {
            valid,
            errors: allErrors,
            warnings: allWarnings
        };
    }
    
    private validateBasicStructure(skill: Skill): ValidationResult {
        const errors: ValidationError[] = [];
        const warnings: ValidationWarning[] = [];
        
        if (!skill.id || skill.id.trim() === '') {
            errors.push({
                field: 'id',
                code: 'EMPTY_ID',
                message: 'Skill ID cannot be empty'
            });
        }
        
        if (!skill.name || skill.name.trim() === '') {
            errors.push({
                field: 'name',
                code: 'EMPTY_NAME',
                message: 'Skill name cannot be empty'
            });
        }
        
        if (!skill.description || skill.description.trim() === '') {
            warnings.push({
                field: 'description',
                code: 'EMPTY_DESCRIPTION',
                message: 'Skill description is empty'
            });
        }
        
        if (!skill.version || !this.isValidVersion(skill.version)) {
            errors.push({
                field: 'version',
                code: 'INVALID_VERSION',
                message: 'Skill version must be in semver format (e.g., 1.0.0)'
            });
        }
        
        return { valid: errors.length === 0, errors, warnings };
    }
    
    private validateInterface(skill: Skill): ValidationResult {
        const errors: ValidationError[] = [];
        const warnings: ValidationWarning[] = [];
        
        if (typeof skill.execute !== 'function') {
            errors.push({
                field: 'execute',
                code: 'MISSING_EXECUTE',
                message: 'Skill must implement execute method'
            });
        }
        
        if (typeof skill.validate !== 'function') {
            errors.push({
                field: 'validate',
                code: 'MISSING_VALIDATE',
                message: 'Skill must implement validate method'
            });
        }
        
        if (typeof skill.getMetadata !== 'function') {
            errors.push({
                field: 'getMetadata',
                code: 'MISSING_GET_METADATA',
                message: 'Skill must implement getMetadata method'
            });
        }
        
        if (!Array.isArray(skill.tags)) {
            errors.push({
                field: 'tags',
                code: 'INVALID_TAGS',
                message: 'Skill tags must be an array'
            });
        }
        
        if (!Array.isArray(skill.supportedIntents)) {
            errors.push({
                field: 'supportedIntents',
                code: 'INVALID_INTENTS',
                message: 'Supported intents must be an array'
            });
        }
        
        if (!Array.isArray(skill.supportedLanguages)) {
            errors.push({
                field: 'supportedLanguages',
                code: 'INVALID_LANGUAGES',
                message: 'Supported languages must be an array'
            });
        }
        
        return { valid: errors.length === 0, errors, warnings };
    }
    
    private validateMetadata(skill: Skill): ValidationResult {
        const errors: ValidationError[] = [];
        const warnings: ValidationWarning[] = [];
        
        const metadata = skill.getMetadata();
        
        if (!metadata.createdAt) {
            warnings.push({
                field: 'createdAt',
                code: 'MISSING_CREATED_AT',
                message: 'Metadata missing createdAt field'
            });
        }
        
        if (!metadata.updatedAt) {
            warnings.push({
                field: 'updatedAt',
                code: 'MISSING_UPDATED_AT',
                message: 'Metadata missing updatedAt field'
            });
        }
        
        if (metadata.tags.length === 0) {
            warnings.push({
                field: 'tags',
                code: 'NO_TAGS',
                message: 'Skill has no tags'
            });
        }
        
        if (metadata.supportedIntents.length === 0) {
            warnings.push({
                field: 'supportedIntents',
                code: 'NO_INTENTS',
                message: 'Skill supports no intents'
            });
        }
        
        return { valid: errors.length === 0, errors, warnings };
    }
    
    private validateDependencies(skill: Skill): ValidationResult {
        const errors: ValidationError[] = [];
        const warnings: ValidationWarning[] = [];
        
        return { valid: errors.length === 0, errors, warnings };
    }
    
    private isValidVersion(version: string): boolean {
        const semverRegex = /^\d+\.\d+\.\d+(-[a-zA-Z0-9.]+)?$/;
        return semverRegex.test(version);
    }
    
    public registerValidator(name: string, validator: (skill: Skill) => ValidationResult): void {
        this.validators.set(name, validator);
        this.logger.debug(`Registered validator: ${name}`);
    }
    
    public unregisterValidator(name: string): void {
        this.validators.delete(name);
        this.logger.debug(`Unregistered validator: ${name}`);
    }
    
    public getRegisteredValidators(): string[] {
        return Array.from(this.validators.keys());
    }
}
