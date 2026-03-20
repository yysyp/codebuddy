# ETL Tagging System - 快速使用指南

## 🚀 快速启动

### 方法1: 使用快速启动脚本

```bash
cd c:\Users\yysyp\CodeBuddy\20260318163401
quick-start.bat
```

这个脚本会：
1. 启动Control Panel (端口8080)
2. 等待20秒
3. 启动Data Panel (端口8081)
4. 自动打开Swagger UI

### 方法2: 手动启动

**终端1 - 启动Control Panel:**
```bash
cd c:\Users\yysyp\CodeBuddy\20260318163401
java -jar etl-control-panel\target\etl-control-panel-1.0.0-SNAPSHOT.jar
```

**终端2 - 启动Data Panel:**
```bash
cd c:\Users\yysyp\CodeBuddy\20260318163401
java -jar etl-data-panel\target\etl-data-panel-1.0.0-SNAPSHOT.jar
```

## 📝 测试API

### 使用测试脚本

```bash
test-api.bat
```

### 手动测试

**1. 检查Control Panel健康状态:**
```bash
curl http://localhost:8080/actuator/health
```

**2. 获取已发布的规则:**
```bash
curl http://localhost:8080/api/v1/rules/published
```

**3. 创建新规则:**
```bash
curl -X POST http://localhost:8080/api/v1/rules ^
  -H "Content-Type: application/json" ^
  -H "X-User: admin" ^
  -d "{\"name\":\"my-rule\",\"description\":\"My custom rule\",\"ruleContent\":\"package com.etl.rules\nimport com.etl.data.model.Transaction\nrule \\\"my-rule\\\" when $t : Transaction(amount > 1000) then $t.addTag(\\\"HIGH_VALUE\\\"); end\",\"ruleType\":\"TAGGING\",\"targetType\":\"TRANSACTION\",\"priority\":\"HIGH\"}"
```

**4. 发布规则:**
```bash
curl -X POST http://localhost:8080/api/v1/rules/1/publish ^
  -H "X-User: admin"
```

**5. 执行数据处理作业:**
```bash
curl -X POST "http://localhost:8081/api/v1/jobs/tagging/execute"
```

## 🌐 Web界面

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:etl_control`
  - Username: `sa`
  - Password: (留空)

## 📊 预置的示例规则

系统启动后会自动创建4个示例规则：

1. **high-amount-transaction**: 标记金额大于10,000的交易
   - Tags: `HIGH_AMOUNT`, `REQUIRES_REVIEW`

2. **fraud-suspicious**: 标记可疑交易
   - Tags: `FRAUD_SUSPICIOUS`, `URGENT_REVIEW`

3. **small-transaction**: 标记小额交易
   - Tags: `SMALL_AMOUNT`

4. **international-transaction**: 标记国际货币交易
   - Tags: `INTERNATIONAL`, `CURRENCY_EXCHANGE`

## 📂 数据文件位置

- **输入数据**: `data/input/transactions.csv`
- **输出数据**: `data/output/tagged_transactions.csv`

## ⚙️ 配置说明

### Control Panel配置 (`etl-control-panel/src/main/resources/application.yml`)
- 服务端口: 8080
- 数据库: H2内存数据库
- Swagger UI: 启用
- H2 Console: 启用

### Data Panel配置 (`etl-data-panel/src/main/resources/application.yml`)
- 服务端口: 8081
- Control Panel URL: http://localhost:8080
- 输入路径: file:///data/input/transactions.csv
- 输出路径: file:///data/output/tagged_transactions.csv

## 🔧 故障排查

### 端口被占用

如果端口8080或8081被占用，修改配置文件中的端口号：
```yaml
server:
  port: 8082  # 修改为可用端口
```

### 应用启动失败

检查日志输出，常见问题：
1. Java版本不匹配 - 需要JDK 17或更高版本
2. 端口冲突 - 更换端口
3. 内存不足 - 增加JVM内存：`java -Xmx1g -jar ...`

### 无法访问API

1. 确认应用已启动
2. 检查防火墙设置
3. 验证URL和端口是否正确

## 📚 API文档

完整的API文档请访问: http://localhost:8080/swagger-ui.html

主要API端点：

**Control Panel (端口 8080):**
- `GET /api/v1/rules` - 获取所有规则
- `POST /api/v1/rules` - 创建规则
- `PUT /api/v1/rules/{id}` - 更新规则
- `POST /api/v1/rules/{id}/publish` - 发布规则
- `GET /api/v1/rules/published` - 获取已发布规则
- `GET /api/v1/schemas` - 获取所有Schema
- `GET /api/v1/sql` - 获取所有SQL定义

**Data Panel (端口 8081):**
- `POST /api/v1/jobs/tagging/execute` - 执行标签作业
- `GET /api/v1/jobs/health` - 健康检查

## 🎯 下一步

1. 访问Swagger UI查看完整API文档
2. 使用测试脚本验证系统功能
3. 创建自定义规则
4. 准备自己的交易数据文件
5. 执行数据处理作业

## 📞 支持

如有问题，请查看：
- 项目README.md
- Swagger UI API文档
- 应用日志输出
