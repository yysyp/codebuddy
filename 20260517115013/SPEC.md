# CodeBuddy Agent 开发助手智能体配置

## 1. 项目概述

**项目名称**: CodeBuddy Dev Assistant Agent  
**项目类型**: VS Code 智能体开发助手  
**核心功能**: 一个可被VS Code自动识别的智能体，具备Skills（技能）和Hooks（钩子）系统，支持自动学习和进化，具备安全防护机制。  
**目标用户**: 开发者

## 2. 系统架构

### 2.1 目录结构

```
.codebuddy/
├── agent/
│   └── agent.toml              # Agent核心配置
├── skills/                      # 技能目录
│   ├── skill-define.toml        # 技能定义注册表
│   └── skills/                  # 具体技能实现
│       ├── code-review/
│       │   ├── manifest.toml    # 技能清单
│       │   └── handler.py      # 技能处理器
│       ├── test-generator/
│       │   ├── manifest.toml
│       │   └── handler.py
│       └── auto-learn/
│           ├── manifest.toml
│           └── handler.py
├── hooks/                       # 钩子目录
│   └── hooks.toml               # 钩子配置
├── memory/                      # 记忆存储
│   ├── conversations/           # 对话记忆
│   ├── patterns/                # 学习到的模式
│   └── skills/                  # 进化后的技能
├── security/
│   └── security.toml            # 安全配置
├── utils/
│   ├── env_loader.py            # 环境变量加载器
│   ├── skill_manager.py          # 技能管理器
│   ├── hook_executor.py         # 钩子执行器
│   └── evolution_engine.py      # 进化引擎
├── config/
│   └── config.toml              # 全局配置
├── build.bat                    # Windows构建脚本
├── build.sh                     # Unix构建脚本
├── run.bat                      # Windows运行脚本
├── run.sh                       # Unix运行脚本
├── requirements.txt             # Python依赖
└── README.md                    # 项目说明
```

### 2.2 Agent配置 (agent.toml)

VS Code会自动扫描`.codebuddy/agent/agent.toml`文件来识别和加载Agent。

**关键配置项**:
- `agent_id`: 唯一标识符
- `name`: 显示名称
- `version`: 版本号
- `capabilities`: 能力列表
- `skills`: 关联的技能列表
- `hooks`: 关联的钩子列表

## 3. 功能需求

### 3.1 Skills（技能）系统

#### 3.1.1 技能定义

每个技能包含：
- `manifest.toml`: 技能清单（名称、描述、触发条件、权限）
- `handler.py`: 技能处理器（Python脚本）
- `evolution_data/`: 进化数据存储

#### 3.1.2 核心技能

1. **代码审查技能 (code-review)**
   - 自动分析代码质量问题
   - 检测安全漏洞
   - 提供优化建议

2. **测试生成技能 (test-generator)**
   - 根据代码自动生成单元测试
   - 覆盖边界情况

3. **自动学习技能 (auto-learn)**
   - 记录用户交互模式
   - 提取可复用的技能
   - 持续优化现有技能

### 3.2 Hooks（钩子）系统

#### 3.2.1 钩子类型

- `pre_agent_run`: Agent执行前触发
- `post_agent_run`: Agent执行后触发
- `on_error`: 错误发生时触发
- `pre_skill_execute`: 技能执行前触发
- `post_skill_execute`: 技能执行后触发

#### 3.2.2 钩子配置

```toml
[hooks]
[[hooks.pre_agent_run]]
name = "security_check"
script = "hooks/security_check.py"
enabled = true

[[hooks.post_agent_run]]
name = "conversation_logger"
script = "hooks/conversation_logger.py"
enabled = true
```

### 3.3 自动进化机制

#### 3.3.1 学习流程

1. **交互记录**: 记录用户与Agent的每次交互
2. **模式提取**: 从交互中识别重复模式
3. **技能生成**: 将有效模式转化为新技能
4. **持续优化**: 根据反馈调整技能参数

#### 3.3.2 进化策略

- 成功率跟踪
- 性能指标监控
- 用户反馈收集
- A/B测试支持

### 3.4 安全机制

#### 3.4.1 敏感信息保护

- **环境变量注入**: 所有账号密码从环境变量读取
- **敏感信息过滤**: 禁止敏感信息传递到LLM
- **日志脱敏**: 日志中自动过滤敏感字段

#### 3.4.2 安全配置

```toml
[security]
# 敏感字段列表（匹配时自动脱敏）
sensitive_fields = ["password", "token", "secret", "key", "credential"]

# 禁止发送到LLM的内容模式
forbidden_patterns = [
    "api[_-]?key",
    "password",
    "secret",
    "bearer\\s+[a-zA-Z0-9]+"
]

# 环境变量白名单（可安全暴露给LLM）
safe_env_vars = ["PATH", "USER", "HOME", "LANG"]

# LLM最大token限制
max_prompt_tokens = 4000
```

## 4. 环境变量设计

### 4.1 必须的环境变量

| 变量名 | 说明 | 来源 |
|--------|------|------|
| `CODEBUDDY_API_KEY` | API密钥 | 环境变量注入 |
| `CODEBUDDY_DB_PASSWORD` | 数据库密码 | 环境变量注入 |
| `CODEBUDDY_SECRET_KEY` | 加密密钥 | 环境变量注入 |

### 4.2 安全加载器

```python
# utils/env_loader.py
import os
from typing import Optional

class SecureEnvLoader:
    """安全的环境变量加载器"""

    SENSITIVE_PREFIXES = ['CODEBUDDY_', 'SECRET_', 'API_', 'PASSWORD_']

    @classmethod
    def get(cls, key: str, default: Optional[str] = None) -> Optional[str]:
        """获取环境变量，自动过滤敏感信息"""
        value = os.environ.get(key, default)

        # 验证变量名是否包含敏感前缀
        if any(key.upper().startswith(prefix) for prefix in cls.SENSITIVE_PREFIXES):
            # 记录访问但不暴露值
            cls._log_sensitive_access(key)

        return value

    @classmethod
    def mask_value(cls, value: str) -> str:
        """遮蔽敏感值"""
        if len(value) <= 4:
            return "****"
        return value[:2] + "*" * (len(value) - 4) + value[-2:]
```

## 5. VS Code集成

### 5.1 自动识别机制

VS Code通过以下方式识别CodeBuddy Agent:
1. 查找`.codebuddy/agent/agent.toml`文件
2. 解析配置获取Agent元信息
3. 加载Skills和Hooks
4. 注册到VS Code扩展上下文

### 5.2 配置文件格式

```toml
[agent]
id = "codebuddy-dev-assistant"
name = "CodeBuddy 开发助手"
version = "1.0.0"
description = "智能开发助手，支持自动学习和技能进化"
author = "CodeBuddy Team"

[agent.capabilities]
conversation = true
code_analysis = true
auto_learn = true
skill_evolution = true

[agent.hooks]
enabled = true
pre_run = "hooks/pre_agent.py"
post_run = "hooks/post_agent.py"
on_error = "hooks/error_handler.py"

[agent.security]
enable_filter = true
mask_sensitive = true
audit_log = true
```

## 6. 验收标准

### 6.1 功能验收

- [ ] Agent配置文件可被VS Code识别
- [ ] Skills系统可正常加载和执行
- [ ] Hooks系统在正确时机触发
- [ ] 自动学习功能记录交互并生成模式
- [ ] 技能进化机制正常工作

### 6.2 安全验收

- [ ] 敏感信息不从环境变量泄露到LLM
- [ ] 日志中敏感字段已脱敏
- [ ] 禁止模式匹配正确阻止敏感信息
- [ ] 审计日志记录所有敏感操作

### 6.3 性能验收

- [ ] Agent加载时间 < 2秒
- [ ] 技能执行响应时间 < 5秒
- [ ] 记忆存储高效压缩

## 7. 技术依赖

- Python 3.10+
- toml (配置文件解析)
- psutil (系统信息)
- loguru (日志)
- marshmallow (配置验证)
