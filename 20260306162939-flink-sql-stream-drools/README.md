# Flink Transaction Tagging

基于 Apache Flink 和 Drools 规则引擎的交易标记应用。支持SQL模式、DataStream模式和Hybrid模式，并支持从DRL文件或CSV表格定义加载规则。

## 功能特性

- **多模式处理**: 支持 SQL、DataStream、Hybrid 三种处理模式
- **规则引擎**: 集成 Drools 实现业务规则与计算逻辑分离
- **双重规则源**: 支持从DRL文件或CSV表格定义加载规则
- **CSV 支持**: 读取 CSV 格式的交易数据，输出带标记的结果
- **灵活部署**: 支持本地运行和集群部署
- **UDF集成**: SQL模式下通过用户定义函数(UDF)调用Drools规则引擎

## 技术栈

- Apache Flink 1.16.3
- Drools 8.44.0.Final
- Spring Boot 3.2.0
- Java 17+

## 快速开始

### 前置要求

- Java 17+ (建议使用 Zulu 或 Azul OpenJDK)
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

1. SQL 模式 (推荐，支持DRL和CSV表格两种规则源)
2. DataStream 模式
3. Hybrid 模式
4. 生成测试数据

#### 方式二：命令行运行

所有运行命令在 JDK 17+ 上都需要添加 JVM 参数：

```bash
java --add-opens=java.base/java.util=ALL-UNNAMED ^
     --add-opens=java.base/java.lang=ALL-UNNAMED ^
     --add-opens=java.base/java.util.concurrent=ALL-UNNAMED ^
     --add-opens=java.base/java.lang.reflect=ALL-UNNAMED ^
     -jar target/flink-transaction-tagging-1.0.0.jar <mode> <input> <output> [rule-source] [rule-path]
```

参数说明：
- `mode`: 处理模式，可选值：`sql`、`datastream`、`hybrid`、`generate`
- `input`: 输入CSV文件路径（generate模式下为输出路径）
- `output`: 输出CSV文件路径（generate模式下不需要）
- `rule-source`: 可选，规则源类型，可选值：`drl`（默认，使用DRL文件）、`table`（使用CSV表格定义）
- `rule-path`: 可选，规则文件路径，默认为 `src/main/resources/rules/transaction-tagging.drl` 或 `src/main/resources/rules/table-rules.csv`

#### 1. 生成测试数据

```bash
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar generate <output-path>
```

示例：
```bash
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar generate target/test-data/test.csv
```

#### 2. SQL 模式 (推荐)

使用 Flink SQL 进行声明式处理，通过 UDF 调用 Drools 规则引擎：

##### 使用 DRL 规则文件（默认）

```bash
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar sql <input-csv> <output-csv>
```

示例：
```bash
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar sql src/main/resources/data/transactions.csv output/tagged_result.csv
```

##### 使用 CSV 表格规则定义

```bash
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar sql <input-csv> <output-csv> table <table-rules-path>
```

示例：
```bash
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar sql src/main/resources/data/transactions.csv output/tagged_result.csv table src/main/resources/rules/table-rules.csv
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
    parallelism: 2               # 并行度
    checkpoint-interval: 60000   # Checkpoint 间隔 (毫秒)
    input-path: ""               # 输入路径
    output-path: ""              # 输出路径
    rule-source: drl             # 规则源: drl (DRL文件) 或 table (CSV表格)
    table-rules-path: rules/table-rules.csv  # 表格规则文件路径
```

### 规则配置

应用支持两种规则配置方式：

#### 1. DRL 规则文件（传统方式）

规则文件位于: `src/main/resources/rules/transaction-tagging.drl`

这种方式直接编写Drools DRL规则文件，适合复杂的规则逻辑。

#### 2. CSV 表格规则定义（推荐）

规则文件位于: `src/main/resources/rules/table-rules.csv`

这种方式使用CSV表格定义规则，更容易维护和管理，业务人员也能理解。

##### 表格规则格式

```csv
rule_name,field_name,operator,threshold_value,tag,priority,condition_type
HIGH_AMOUNT,amount,>,10000,HIGH_AMOUNT,5,simple
VERY_HIGH_AMOUNT,amount,>,50000,VERY_HIGH_AMOUNT,10,simple
HIGH_RISK,risk_score,>,50,HIGH_RISK,5,simple
INTERNATIONAL,country_code,<>,US,INTERNATIONAL,3,simple
```

字段说明：
- `rule_name`: 规则名称（唯一标识）
- `field_name`: 要检查的字段名（amount, risk_score, country_code, transaction_type等）
- `operator`: 比较操作符（>, >=, <, <=, =, !=, in, not_in）
- `threshold_value`: 阈值（数值、字符串或逗号分隔的列表）
- `tag`: 匹配时添加的标签
- `priority`: 优先级（数字越大优先级越高，用于primary_tag选择）
- `condition_type`: 条件类型（simple: 简单条件, complex: 复杂条件组合）

支持的比较操作符：
- `>` 大于
- `>=` 大于等于
- `<` 小于
- `<=` 小于等于
- `=` 等于
- `!=` 不等于
- `in` 包含在列表中（用于枚举值）
- `not_in` 不包含在列表中

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
- `account_id`: 账户ID
- `amount`: 交易金额
- `currency`: 货币类型
- `transaction_type`: 交易类型 (TRANSFER, PAYMENT, DEBIT, CREDIT, REFUND)
- `counterparty_id`: 对手方ID
- `counterparty_name`: 对手方名称
- `description`: 交易描述
- `transaction_time`: 交易时间
- `country_code`: 国家代码
- `ip_address`: IP地址
- `device_id`: 设备ID
- `risk_score`: 风险评分 (0-100)

### 输出数据格式 (CSV)

输出文件包含以下字段（在输入字段基础上增加）：

```csv
transaction_id,account_id,amount,currency,transaction_type,counterparty_id,counterparty_name,description,transaction_time,country_code,ip_address,device_id,risk_score,tags,primary_tag,tag_count,processing_time,trace_id
TXN-001,ACC001,150.00,USD,DEBIT,MERCH001,Amazon,Online purchase,2024-01-15T10:30:00Z,US,192.168.1.1,DEV001,25,"DEBIT",DEBIT,1,2024-01-15T10:30:35.000Z,FLINK-abc123
```

新增字段说明：
- `tags`: 所有匹配的规则标签，用逗号分隔
- `primary_tag`: 主要标签（优先级最高的标签）
- `tag_count`: 匹配的标签总数
- `processing_time`: 处理时间戳
- `trace_id`: 追踪ID，用于问题排查

**注意**: Flink CSV输出可能包含一些空列，实际使用时请关注tags、primary_tag和tag_count这三个核心字段。

### 示例输出

```csv
TXN-001,ACC001,150.00,USD,DEBIT,MERCH001,Amazon,Online purchase,2024-01-15T10:30:00Z,US,192.168.1.1,DEV001,25,"DEBIT",DEBIT,1,2024-01-15T10:30:35.000Z,FLINK-abc123
TXN-002,ACC002,25000.00,USD,TRANSFER,MERCH002,Bank Transfer,Wire transfer,2024-01-15T11:00:00Z,US,192.168.1.2,DEV002,85,"SUSPICIOUS,VERY_HIGH_RISK,HIGH_AMOUNT,HIGH_RISK,TRANSFER",SUSPICIOUS,5,2024-01-15T11:05:40.000Z,FLINK-def456
TXN-003,ACC003,5.50,USD,DEBIT,MERCH003,Starbucks,Coffee purchase,2024-01-15T11:15:00Z,US,192.168.1.3,DEV001,10,"DEBIT,LOW_AMOUNT",DEBIT,2,2024-01-15T11:20:45.000Z,FLINK-ghi789
TXN-004,ACC001,75000.00,EUR,TRANSFER,MERCH004,International Bank,International wire,2024-01-15T12:00:00Z,DE,192.168.1.4,DEV003,95,"HIGH_RISK_INTERNATIONAL,SUSPICIOUS,CRITICAL_RISK,VERY_HIGH_RISK,VERY_HIGH_AMOUNT,HIGH_AMOUNT,HIGH_RISK,TRANSFER,INTERNATIONAL",HIGH_RISK_INTERNATIONAL,9,2024-01-15T12:05:50.000Z,FLINK-jkl012
```

结果说明：
- TXN-001: 普通借记交易，无特殊标签
- TXN-002: 高额高风险转账，标记为可疑交易
- TXN-003: 低额借记交易，标记为低金额
- TXN-004: 高风险国际转账，匹配9个标签

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

规则文件必须位于：
- DRL文件: `src/main/resources/rules/transaction-tagging.drl`
- CSV表格: `src/main/resources/rules/table-rules.csv`

### 5. CSV输出格式问题

Flink的CSV文件输出可能会包含一些空列，这是正常的。重点关注以下字段：
- `tags`: 所有匹配的规则标签（逗号分隔）
- `primary_tag`: 主要标签（优先级最高的标签）
- `tag_count`: 匹配的标签总数

### 6. 如何选择规则源？

- **DRL文件**: 适合复杂的规则逻辑，需要开发人员维护
- **CSV表格**: 适合简单的条件规则，业务人员也能理解，推荐使用

### 7. 如何自定义规则？

使用CSV表格定义规则更简单：

1. 打开 `src/main/resources/rules/table-rules.csv`
2. 添加或修改规则行
3. 重新编译并运行应用

示例：添加一个新规则检测中金额交易
```csv
MEDIUM_AMOUNT,amount,>=,5000,MEDIUM_AMOUNT,3,simple
```

### 8. 性能优化建议

- 对于大批量数据处理，可以调整 `flink.job.parallelism` 参数增加并行度
- 使用 CSV 表格规则比 DRL 文件解析更快
- 确保 JVM 堆内存足够：`-Xmx2g -Xms2g`

## 快速参考

### 常用命令

```bash
# 编译项目
mvn clean package -DskipTests

# 运行 SQL 模式 (使用 DRL 规则)
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar sql input.csv output.csv

# 运行 SQL 模式 (使用 CSV 表格规则)
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar sql input.csv output.csv table src/main/resources/rules/table-rules.csv

# 运行 DataStream 模式
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar datastream input.csv output.csv

# 生成测试数据
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar generate test.csv
```

### 文件结构

```
项目根目录/
├── build.bat                          # 编译脚本
├── run.bat                            # 运行脚本
├── pom.xml                            # Maven 配置
├── README.md                          # 项目文档
├── src/
│   └── main/
│       ├── java/com/example/flink/
│       │   ├── TransactionTaggingApplication.java  # 主应用入口
│       │   ├── config/
│       │   │   └── FlinkJobConfig.java             # Flink 配置
│       │   ├── function/
│       │   │   └── DroolsTaggingUDF.java           # Drools UDF
│       │   ├── job/
│       │   │   └── TransactionTaggingJob.java       # 作业执行
│       │   ├── model/
│       │   │   └── Transaction.java                 # 交易模型
│       │   ├── service/
│       │   │   ├── RuleEngineService.java          # 规则引擎服务
│       │   │   └── TableRuleParserService.java     # 表格规则解析
│       │   └── util/
│       │       └── CsvUtils.java                    # CSV 工具
│       └── resources/
│           ├── application.yml                      # 应用配置
│           ├── rules/
│           │   ├── transaction-tagging.drl          # DRL 规则
│           │   └── table-rules.csv                  # CSV 表格规则
│           └── data/
│               └── transactions.csv                 # 测试数据
└── output/                           # 输出目录
```

## 许可证

MIT License
