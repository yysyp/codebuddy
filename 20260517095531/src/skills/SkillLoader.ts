import * as path from 'path';
import * as glob from 'glob';
import { Logger } from '../utils/Logger';
import { Skill } from './types/Skill';

export class SkillLoader {
    private readonly logger: Logger;
    private readonly skillsDirectory: string;
    
    constructor() {
        this.logger = Logger.getInstance('SkillLoader');
        this.skillsDirectory = path.join(__dirname, 'skills');
    }
    
    public async loadAllSkills(): Promise<Skill[]> {
        this.logger.info('Loading all skills...');
        
        const skills: Skill[] = [];
        
        const skillFiles = await this.findSkillFiles();
        
        for (const file of skillFiles) {
            try {
                const skill = await this.loadSkill(file);
                if (skill) {
                    skills.push(skill);
                }
            } catch (error) {
                this.logger.error(`Failed to load skill from ${file}`, error);
            }
        }
        
        this.logger.info(`Loaded ${skills.length} skills`);
        
        return skills;
    }
    
    private async findSkillFiles(): Promise<string[]> {
        const patterns = [
            path.join(this.skillsDirectory, '**/*Skill.js'),
            path.join(this.skillsDirectory, '**/*Skill.ts')
        ];
        
        const files: string[] = [];
        
        for (const pattern of patterns) {
            const matches = glob.sync(pattern);
            files.push(...matches);
        }
        
        return files;
    }
    
    public async loadSkill(filePath: string): Promise<Skill | null> {
        this.logger.debug(`Loading skill from ${filePath}`);
        
        try {
            const module = await import(filePath);
            
            const ExportedClass = module.default || module.Skill;
            
            if (!ExportedClass) {
                this.logger.warn(`No skill class found in ${filePath}`);
                return null;
            }
            
            const skill = new ExportedClass();
            
            if (!this.validateSkillInterface(skill)) {
                this.logger.warn(`Skill from ${filePath} does not implement Skill interface correctly`);
                return null;
            }
            
            this.logger.info(`Loaded skill: ${skill.id} (${skill.name})`);
            
            return skill;
        } catch (error) {
            this.logger.error(`Error loading skill from ${filePath}`, error);
            return null;
        }
    }
    
    private validateSkillInterface(skill: unknown): skill is Skill {
        const s = skill as Skill;
        
        return (
            typeof s.id === 'string' &&
            typeof s.name === 'string' &&
            typeof s.description === 'string' &&
            typeof s.version === 'string' &&
            Array.isArray(s.tags) &&
            Array.isArray(s.supportedIntents) &&
            Array.isArray(s.supportedLanguages) &&
            typeof s.execute === 'function' &&
            typeof s.validate === 'function' &&
            typeof s.getMetadata === 'function'
        );
    }
    
    public async loadSkillFromClass(skillClass: new () => Skill): Promise<Skill> {
        const skill = new skillClass();
        
        if (!this.validateSkillInterface(skill)) {
            throw new Error('Invalid skill class');
        }
        
        return skill;
    }
    
    public async loadSkillsFromDirectory(directory: string): Promise<Skill[]> {
        this.logger.info(`Loading skills from directory: ${directory}`);
        
        const skills: Skill[] = [];
        const pattern = path.join(directory, '**/*Skill.js');
        const files = glob.sync(pattern);
        
        for (const file of files) {
            const skill = await this.loadSkill(file);
            if (skill) {
                skills.push(skill);
            }
        }
        
        return skills;
    }
}
