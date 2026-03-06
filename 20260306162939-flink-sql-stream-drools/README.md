# Flink Transaction Tagging

基于 Apache Flink 和 Drools 规则引擎的交易标记应用。

## 功能特性

- **多模式处理**: 支持 SQL、DataStream、Hybrid 三种处理模式
- **规则引擎**: 集成 Drools 实现业务规则与计算逻辑分离
- **CSV 支持**: 读取 CSV 格式的交易数据，输出带标记的结果
- **灵活部署**: 支持本地运行和集群部署

## 技术栈

- Apache Flink 1.16.3
- Drools 8.44.0.Final
- Spring Boot 3.2.0
- Java 17

## 快速开始

### 前置要求

- Java 17+
- Maven 3.8+

### 编译打包

#### 方式一：使用 Maven 命令

```bash
mvn clean package -DskipTests
```

#### 方式二：使用批处理脚本 (Windows)

```bash
# 双击运行 build.bat 即可编译
build.bat
```

### 运行应用

#### 方式一：使用批处理脚本 (推荐)

双击运行 `run.bat`，根据提示选择模式：

1. SQL 模式 (推荐)
2. DataStream 模式
3. Hybrid 模式
4. 生成测试数据

#### 方式二：命令行运行

所有运行命令在 JDK 17 上都需要添加 JVM 参数：

```bash
java --add-opens=java.base/java.util=ALL-UNNAMED ^
     --add-opens=java.base/java.lang=ALL-UNNAMED ^
     --add-opens=java.base/java.util.concurrent=ALL-UNNAMED ^
     -jar target/flink-transaction-tagging-1.0.0.jar <mode> <input> <output>
```

#### 1. 生成测试数据

```bash
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar generate <output-path>
```

示例：
```bash
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar generate target/test-data/test.csv
```

#### 2. SQL 模式 (推荐)

使用 Flink SQL 进行声明式处理：

```bash
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar sql <input-csv> <output-csv>
```

示例：
```bash
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar sql src/main/resources/data/transactions.csv output/tagged_result.csv
```

#### 3. DataStream 模式

使用 DataStream API 与 Drools 规则引擎集成：

```bash
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar datastream <input-csv> <output-csv>
```

#### 4. Hybrid 模式

结合 SQL 和 DataStream API：

```bash
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar hybrid <input-csv> <output-csv>
```

## 配置说明

### 应用配置 (application.yml)

```yaml
flink:
  job:
    mode: sql                    # 处理模式: sql, datastream, hybrid
    parallelism: 1               # 并行度
    checkpoint-interval: 60000   # Checkpoint 间隔 (毫秒)
    input-path: ""               # 输入路径
    output-path: ""              # 输出路径
```

### 规则配置

规则文件位于: `src/main/resources/rules/transaction-tagging.drl`

#### 内置规则

| 规则名称 | 描述 | 标签 |
|---------|------|------|
| HIGH_AMOUNT | 金额 > 10000 | HIGH_AMOUNT |
| VERY_HIGH_AMOUNT | 金额 > 50000 | VERY_HIGH_AMOUNT |
| HIGH_RISK | 风险评分 > 50 | HIGH_RISK |
| CRITICAL_RISK | 风险评分 > 75 | CRITICAL_RISK |
| VERY_HIGH_RISK | 风险评分 > 90 | VERY_HIGH_RISK |
| TRANSFER | 转账交易 | TRANSFER |
| PAYMENT | 支付交易 | PAYMENT |
| DEBIT | 借记交易 | DEBIT |
| CREDIT | 贷记交易 | CREDIT |
| REFUND | 退款交易 | REFUND |
| INTERNATIONAL | 国际交易 | INTERNATIONAL |
| NO_COUNTRY | 无国家信息 | NO_COUNTRY |
| SUSPICIOUS | 可疑交易 | SUSPICIOUS |
| HIGH_RISK_INTERNATIONAL | 高风险国际交易 | HIGH_RISK_INTERNATIONAL |

### 输入数据格式 (CSV)

```csv
transaction_id,timestamp,amount,currency,transaction_type,risk_score,country,merchant_name,account_id
TXN001,2024-01-15T10:30:00,15000.00,USD,TRANSFER,85,US,ABC Corp,ACC123
TXN002,2024-01-15T11:45:00,250.50,CNY,PAYMENT,20,CN,XYZ Store,ACC456
```

字段说明：
- `transaction_id`: 交易唯一标识
- `timestamp`: 交易时间
- `amount`: 交易金额
- `currency`: 货币类型
- `transaction_type`: 交易类型 (TRANSFER, PAYMENT, DEBIT, CREDIT, REFUND)
- `risk_score`: 风险评分 (0-100)
- `country`: 国家代码
- `merchant_name`: 商户名称
- `account_id`: 账户ID

### 输出数据格式 (CSV)

```csv
transaction_id,timestamp,amount,currency,transaction_type,risk_score,country,merchant_name,account_id,tags
TXN001,2024-01-15T10:30:00,15000.00,USD,TRANSFER,85,US,ABC Corp,ACC123,"HIGH_AMOUNT,HIGH_RISK,TRANSFER"
```

输出在输入基础上增加 `tags` 字段，包含所有匹配的规则标签。

## 运行测试

```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=RuleEngineServiceTest
```

## 项目结构

```
src/
├── main/
│   ├── java/com/example/flink/
│   │   ├── TransactionTaggingApplication.java  # 主应用入口
│   │   ├── config/
│   │   │   └── FlinkJobConfig.java             # Flink 配置类
│   │   ├── function/
│   │   │   ├── TransactionTaggingFunction.java # DataStream 处理函数
│   │   │   └── TaggingScalarFunction.java      # Flink SQL 标量函数
│   │   ├── job/
│   │   │   └── TransactionTaggingJob.java       # 作业执行逻辑
│   │   ├── model/
│   │   │   ├── Transaction.java                 # 交易数据模型
│   │   │   ├── TaggedTransaction.java          # 带标记的交易
│   │   │   └── TaggingResult.java              # 标记结果
│   │   ├── service/
│   │   │   └── RuleEngineService.java           # Drools 规则引擎服务
│   │   └── util/
│   │       ├── CsvUtils.java                    # CSV 工具类
│   │       └── TraceIdGenerator.java            # 追踪ID生成器
│   └── resources/
│       ├── application.yml                      # 应用配置
│       ├── logback.xml                           # 日志配置
│       ├── rules/
│       │   └── transaction-tagging.drl         # Drools 规则文件
│       └── data/
│           └── transactions.csv                 # 示例数据
└── test/
    └── java/com/example/flink/                  # 单元测试
```

## 常见问题

### 1. JDK 17+ 序列化问题

Flink 使用 Kryo 序列化，在 JDK 17+ 上会遇到模块化限制，需要添加 JVM 参数：

```bash
java --add-opens=java.base/java.util=ALL-UNNAMED ^
     --add-opens=java.base/java.lang=ALL-UNNAMED ^
     --add-opens=java.base/java.util.concurrent=ALL-UNNAMED ^
     -jar target/flink-transaction-tagging-1.0.0.jar <mode> <input> <output>
```

或者使用 `run.bat` 脚本，已自动包含这些参数。

### 2. 编译错误 (ExceptionInInitializerError)

如果遇到 Maven 编译错误，确保 `pom.xml` 中的编译器插件配置包含：

```xml
<release>17</release>
<fork>true</fork>
<compilerArgs>
    <arg>--add-opens=java.base/java.lang=ALL-UNNAMED</arg>
    <arg>--add-opens=java.base/java.util=ALL-UNNAMED</arg>
</compilerArgs>
```

### 3. Checkpoint 失败

确保输出目录存在且有写入权限：

```bash
mkdir output
```

### 4. 规则文件找不到

规则文件必须位于 `src/main/resources/rules/transaction-tagging.drl` 或项目根目录的 `src/main/resources/rules/` 目录下。

## 许可证

MIT License
