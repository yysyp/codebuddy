package com.etl.control.config;

import com.etl.control.entity.RuleDefinition;
import com.etl.control.entity.RuleDefinition.RuleStatus;
import com.etl.control.entity.SchemaDefinition;
import com.etl.control.entity.SqlDefinition;
import com.etl.control.entity.SqlDefinition.SqlType;
import com.etl.control.repository.RuleDefinitionRepository;
import com.etl.control.repository.SchemaDefinitionRepository;
import com.etl.control.repository.SqlDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Data Initializer
 * Initializes sample data for testing
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RuleDefinitionRepository ruleRepository;
    private final SchemaDefinitionRepository schemaRepository;
    private final SqlDefinitionRepository sqlRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing sample data...");
        
        initSchemas();
        initRules();
        initSqlDefinitions();
        
        log.info("Sample data initialization completed");
    }

    private void initSchemas() {
        if (schemaRepository.count() == 0) {
            log.info("Creating sample schemas...");
            
            // Transaction Schema
            String transactionSchema = """
                {
                  "fields": [
                    {"name": "transaction_id", "type": "string"},
                    {"name": "user_id", "type": "string"},
                    {"name": "amount", "type": "decimal"},
                    {"name": "currency", "type": "string"},
                    {"name": "transaction_type", "type": "string"},
                    {"name": "timestamp", "type": "timestamp"},
                    {"name": "merchant_id", "type": "string"},
                    {"name": "location", "type": "string"},
                    {"name": "status", "type": "string"}
                  ]
                }
                """;
            
            SchemaDefinition transactionSchemaDef = SchemaDefinition.builder()
                    .name("transaction-schema")
                    .description("Transaction data schema for tagging")
                    .schemaContent(transactionSchema)
                    .status(SchemaDefinition.SchemaStatus.ACTIVE)
                    .schemaType("CSV")
                    .createdBy("system")
                    .updatedBy("system")
                    .build();
            
            schemaRepository.save(transactionSchemaDef);
            log.info("Transaction schema created");
        }
    }

    private void initRules() {
        if (ruleRepository.count() == 0) {
            log.info("Creating sample rules...");
            
            // High Amount Transaction Rule
            String highAmountRule = """
                package com.etl.rules
                
                import com.etl.data.model.Transaction
                
                rule "high-amount-transaction"
                    dialect "mvel"
                    salience 100
                    when
                        $transaction : Transaction(amount > 10000.00)
                    then
                        $transaction.addTag("HIGH_AMOUNT");
                        $transaction.addTag("REQUIRES_REVIEW");
                    end
                """;
            
            RuleDefinition highAmountRuleDef = RuleDefinition.builder()
                    .name("high-amount-transaction")
                    .description("Tag transactions with amount greater than 10,000")
                    .ruleContent(highAmountRule)
                    .version(1)
                    .status(RuleStatus.PUBLISHED)
                    .ruleType("TAGGING")
                    .targetType("TRANSACTION")
                    .priority("HIGH")
                    .tags("amount,review")
                    .createdBy("system")
                    .updatedBy("system")
                    .build();
            
            // Fraud Suspicious Rule
            String fraudRule = """
                package com.etl.rules
                
                import com.etl.data.model.Transaction
                
                rule "fraud-suspicious"
                    dialect "mvel"
                    salience 200
                    when
                        $transaction : Transaction(
                            amount > 5000.00 && 
                            (transactionType == "WITHDRAWAL" || transactionType == "TRANSFER") &&
                            location != "KNOWN_LOCATION"
                        )
                    then
                        $transaction.addTag("FRAUD_SUSPICIOUS");
                        $transaction.addTag("URGENT_REVIEW");
                    end
                """;
            
            RuleDefinition fraudRuleDef = RuleDefinition.builder()
                    .name("fraud-suspicious")
                    .description("Tag suspicious transactions that might be fraudulent")
                    .ruleContent(fraudRule)
                    .version(1)
                    .status(RuleStatus.PUBLISHED)
                    .ruleType("TAGGING")
                    .targetType("TRANSACTION")
                    .priority("CRITICAL")
                    .tags("fraud,security")
                    .createdBy("system")
                    .updatedBy("system")
                    .build();
            
            // Small Transaction Rule
            String smallAmountRule = """
                package com.etl.rules
                
                import com.etl.data.model.Transaction
                
                rule "small-transaction"
                    dialect "mvel"
                    salience 50
                    when
                        $transaction : Transaction(amount < 100.00)
                    then
                        $transaction.addTag("SMALL_AMOUNT");
                    end
                """;
            
            RuleDefinition smallAmountRuleDef = RuleDefinition.builder()
                    .name("small-transaction")
                    .description("Tag transactions with amount less than 100")
                    .ruleContent(smallAmountRule)
                    .version(1)
                    .status(RuleStatus.PUBLISHED)
                    .ruleType("TAGGING")
                    .targetType("TRANSACTION")
                    .priority("LOW")
                    .tags("amount")
                    .createdBy("system")
                    .updatedBy("system")
                    .build();
            
            // International Transaction Rule
            String internationalRule = """
                package com.etl.rules
                
                import com.etl.data.model.Transaction
                
                rule "international-transaction"
                    dialect "mvel"
                    salience 80
                    when
                        $transaction : Transaction(
                            currency != "USD" && currency != "CNY"
                        )
                    then
                        $transaction.addTag("INTERNATIONAL");
                        $transaction.addTag("CURRENCY_EXCHANGE");
                    end
                """;
            
            RuleDefinition internationalRuleDef = RuleDefinition.builder()
                    .name("international-transaction")
                    .description("Tag international currency transactions")
                    .ruleContent(internationalRule)
                    .version(1)
                    .status(RuleStatus.PUBLISHED)
                    .ruleType("TAGGING")
                    .targetType("TRANSACTION")
                    .priority("MEDIUM")
                    .tags("international,currency")
                    .createdBy("system")
                    .updatedBy("system")
                    .build();
            
            ruleRepository.saveAll(List.of(
                    highAmountRuleDef,
                    fraudRuleDef,
                    smallAmountRuleDef,
                    internationalRuleDef
            ));
            
            log.info("Sample rules created");
        }
    }

    private void initSqlDefinitions() {
        if (sqlRepository.count() == 0) {
            log.info("Creating sample SQL definitions...");
            
            // Source Table SQL
            String sourceTableSql = """
                CREATE TABLE transactions (
                    transaction_id STRING,
                    user_id STRING,
                    amount DECIMAL(18, 2),
                    currency STRING,
                    transaction_type STRING,
                    transaction_time TIMESTAMP(3),
                    merchant_id STRING,
                    location STRING,
                    status STRING,
                    WATERMARK FOR transaction_time AS transaction_time - INTERVAL '5' SECOND
                ) WITH (
                    'connector' = 'filesystem',
                    'path' = 'file:///data/input/transactions.csv',
                    'format' = 'csv',
                    'csv.ignore-parse-errors' = 'true',
                    'csv.allow-comments' = 'true'
                )
                """;
            
            SqlDefinition sourceSql = SqlDefinition.builder()
                    .name("transaction-source-table")
                    .description("Source table for reading transaction data from CSV")
                    .sqlContent(sourceTableSql)
                    .sqlType(SqlType.SOURCE_TABLE)
                    .status(SqlDefinition.SqlStatus.ACTIVE)
                    .associatedSchema("transaction-schema")
                    .createdBy("system")
                    .updatedBy("system")
                    .build();
            
            // Sink Table SQL
            String sinkTableSql = """
                CREATE TABLE tagged_transactions (
                    transaction_id STRING,
                    user_id STRING,
                    amount DECIMAL(18, 2),
                    currency STRING,
                    transaction_type STRING,
                    transaction_time TIMESTAMP(3),
                    merchant_id STRING,
                    location STRING,
                    status STRING,
                    tags STRING
                ) WITH (
                    'connector' = 'filesystem',
                    'path' = 'file:///data/output/tagged_transactions.csv',
                    'format' = 'csv'
                )
                """;
            
            SqlDefinition sinkSql = SqlDefinition.builder()
                    .name("tagged-transaction-sink-table")
                    .description("Sink table for writing tagged transaction data to CSV")
                    .sqlContent(sinkTableSql)
                    .sqlType(SqlType.SINK_TABLE)
                    .status(SqlDefinition.SqlStatus.ACTIVE)
                    .associatedSchema("transaction-schema")
                    .createdBy("system")
                    .updatedBy("system")
                    .build();
            
            sqlRepository.saveAll(List.of(sourceSql, sinkSql));
            log.info("Sample SQL definitions created");
        }
    }
}
