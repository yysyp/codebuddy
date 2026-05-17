# VS Code Extension Developer Extension Quick Start

## What's in the folder

* This folder contains all of the files necessary for your extension.
* `package.json` - this is the manifest file that defines the location of the snapshot file and specifies the base set of contributions.
* `src/extension.ts` - this is the main file where you will provide examples (e.g. a callback for activating the extension).

## Get up and running straight away

* Press `F5` to open a window with the extension loaded.
* Run your command from the command palette by pressing (`Ctrl+Shift+P` or `Cmd+Shift+P` on Mac) and typing `Developer Assistant: Start`.
* Check the `DEBUG CONSOLE` to see logs while the extension works.

## Make changes

* Changes to the source code will immediately compile and take effect in the running extension (no need to reload).
* You can also open the debug console to see logs.

## Adopt advanced topics

* [Global State](https://code.visualstudio.com/api/extension-capabilities/common-capabilities#global-state)
* [Data Storage](https://code.visualstudio.com/api/extension-capabilities/common-capabilities#data-storage)
* [Event-based Contributions](https://code.visualstudio.com/api/extension-capabilities/common-capabilities#event-based-contributions)
* [Telemetry](https://code.visualstudio.com/api/extension-capabilities/common-capabilities#telemetry)
* [Themes](https://code.visualstudio.com/api/extension-capabilities/theming)
* [Breadcrumbs](https://code.visualstudio.com/api/extension-capabilities/breadcrumbs)
* [WebViews](https://code.visualstudio.com/api/extension-capabilities/webview)

## Explore the source code

### Agent Core Module (`src/agent/`)
- `AgentCore.ts` - Main agent engine that orchestrates all components
- `AgentContext.ts` - Manages context across interactions
- `AgentConfig.ts` - Configuration management
- `IntentRecognizer.ts` - Recognizes user intent from natural language
- `ResponseGenerator.ts` - Generates responses using skills

### Skills System (`src/skills/`)
- `SkillRegistry.ts` - Central registry for all skills
- `SkillLoader.ts` - Dynamically loads and unloads skills
- `SkillExecutor.ts` - Executes skill operations
- `SkillEvolution.ts` - Manages skill evolution and optimization
- `SkillValidator.ts` - Validates skill implementations

### Hooks System (`src/hooks/`)
- `HookManager.ts` - Manages hook lifecycle
- `HookRegistry.ts` - Registry for hook implementations
- Various hook implementations in `hooks/` folder

### Security System (`src/security/`)
- `SensitiveDataFilter.ts` - Filters sensitive information
- `PrivacyManager.ts` - Manages privacy settings
- `SecureStorage.ts` - Secure storage for sensitive data
- `DataSanitizer.ts` - Sanitizes data before processing

### Learning Engine (`src/learning/`)
- `InteractionLearner.ts` - Learns from user interactions
- `PatternAnalyzer.ts` - Analyzes usage patterns
- `SkillOptimizer.ts` - Optimizes skill performance
- `EvolutionEngine.ts` - Orchestrates the evolution process

## Useful resources

* [Extension API Documentation](https://code.visualstudio.com/api)
* [TypeScript in VS Code](https://code.visualstudio.com/docs/languages/typescript)
* [TypeScript Language Specification](https://github.com/microsoft/TypeScript/blob/main/doc/spec.md)

## Next Steps

### Creating a production-ready extension

1. **Implement your skills** - Create custom skills in `src/skills/skills/`
2. **Add hooks** - Register hooks in `src/hooks/hooks/`
3. **Configure security** - Set up security filters in `src/security/`
4. **Enable learning** - Configure learning parameters in `src/learning/`
5. **Test thoroughly** - Write tests in `src/test/`

### Publishing your extension

1. Update `package.json` with your publisher name
2. Run `npm install` to install dependencies
3. Run `npm run compile` to compile
4. Run `vsce package` to create .vsix file
5. Publish using `vsce publish` or through VS Code Extension Market

---

**Enjoy!**
