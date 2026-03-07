package com.example.flink.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for parsing table-structured rules and converting them to Drools DRL format.
 * Supports CSV-based rule definitions.
 */
@Slf4j
@Service
public class TableRuleParserService {

    private static final String RULES_FILE = "rules/table-rules.csv";

    /**
     * Parse table-structured rules from CSV file and convert to DRL format
     *
     * @return DRL rule content
     */
    public String parseRulesFromTable() throws Exception {
        log.info("Parsing table-structured rules from: {}", RULES_FILE);

        List<RuleDefinition> rules = readRuleDefinitions();
        String drlContent = convertToDrl(rules);

        log.info("Successfully parsed {} rules from table structure", rules.size());
        return drlContent;
    }

    /**
     * Read rule definitions from CSV file
     */
    private List<RuleDefinition> readRuleDefinitions() throws Exception {
        List<RuleDefinition> rules = new ArrayList<>();

        InputStream is = getClass().getClassLoader().getResourceAsStream(RULES_FILE);
        if (is == null) {
            // Try file system path
            java.nio.file.Path path = java.nio.file.Path.of("src/main/resources/" + RULES_FILE);
            if (java.nio.file.Files.exists(path)) {
                is = java.nio.file.Files.newInputStream(path);
            } else {
                throw new Exception("Rules file not found: " + RULES_FILE);
            }
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            // Skip header line
            String line = reader.readLine();
            if (line == null) {
                throw new Exception("Empty rules file");
            }

            log.debug("Header: {}", line);

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                try {
                    RuleDefinition rule = parseRuleLine(line);
                    rules.add(rule);
                    log.debug("Parsed rule: {}", rule);
                } catch (Exception e) {
                    log.warn("Failed to parse rule line: {} - {}", line, e.getMessage());
                }
            }
        }

        return rules;
    }

    /**
     * Parse a single rule line from CSV
     */
    private RuleDefinition parseRuleLine(String line) {
        String[] parts = parseCsvLine(line);

        if (parts.length < 5) {
            throw new IllegalArgumentException("Invalid rule line, expected at least 5 fields: " + line);
        }

        RuleDefinition rule = new RuleDefinition();
        rule.setRuleName(parts[0].trim());
        rule.setFieldName(parts[1].trim());
        rule.setOperator(parts[2].trim());
        rule.setThresholdValue(parts[3].trim());
        rule.setTag(parts[4].trim());

        if (parts.length > 5 && !parts[5].trim().isEmpty()) {
            rule.setPriority(Integer.parseInt(parts[5].trim()));
        }

        if (parts.length > 6) {
            rule.setConditionType(parts[6].trim());
        }

        // Handle compound rules
        if (rule.getConditionType() != null && rule.getConditionType().equals("compound")) {
            if (parts.length > 7) {
                rule.setSecondFieldName(parts[5].trim());
                rule.setSecondOperator(parts[6].trim());
                rule.setSecondThresholdValue(parts[7].trim());
                // Move priority to correct position
                if (parts.length > 8) {
                    rule.setPriority(Integer.parseInt(parts[8].trim()));
                }
            }
        }

        return rule;
    }

    /**
     * Parse CSV line handling quoted values
     */
    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());

        return result.toArray(new String[0]);
    }

    /**
     * Convert rule definitions to DRL format
     */
    private String convertToDrl(List<RuleDefinition> rules) {
        StringBuilder drl = new StringBuilder();

        // Add header
        drl.append("package rules;\n\n");
        drl.append("import com.example.flink.model.Transaction;\n");
        drl.append("import java.math.BigDecimal;\n");
        drl.append("import java.util.List;\n");
        drl.append("import java.util.ArrayList;\n");
        drl.append("import org.slf4j.Logger;\n\n");
        drl.append("global Logger log;\n");
        drl.append("global String traceId;\n\n");

        // Convert each rule
        for (RuleDefinition rule : rules) {
            drl.append(convertRuleToDrl(rule));
            drl.append("\n");
        }

        return drl.toString();
    }

    /**
     * Convert a single rule definition to DRL format
     */
    private String convertRuleToDrl(RuleDefinition rule) {
        StringBuilder drl = new StringBuilder();

        drl.append("// ============================================================\n");
        drl.append("// Rule: ").append(rule.getRuleName()).append("\n");
        drl.append("// Field: ").append(rule.getFieldName()).append(" ").append(rule.getOperator());
        drl.append(" ").append(rule.getThresholdValue()).append("\n");
        drl.append("// Tag: ").append(rule.getTag()).append("\n");
        drl.append("// ============================================================\n");

        drl.append("rule \"").append(rule.getRuleName()).append("\"\n");
        drl.append("    salience ").append(rule.getPriority()).append("\n");
        drl.append("    when\n");

        if ("compound".equals(rule.getConditionType())) {
            // Compound condition
            drl.append("        $tx : Transaction(");
            drl.append(rule.getFieldName()).append(" != null, ");
            drl.append(rule.getFieldName()).append(" ").append(rule.getOperator()).append(" ");
            drl.append(formatValue(rule.getFieldName(), rule.getThresholdValue())).append(", ");
            drl.append(rule.getSecondFieldName()).append(" != null, ");
            drl.append(rule.getSecondFieldName()).append(" ").append(rule.getSecondOperator()).append(" ");
            drl.append(formatValue(rule.getSecondFieldName(), rule.getSecondThresholdValue()));
            drl.append(")\n");
        } else if ("nullable".equals(rule.getConditionType())) {
            // Null check
            drl.append("        $tx : Transaction(").append(rule.getFieldName()).append(" == null)\n");
        } else if ("empty".equals(rule.getConditionType())) {
            // Empty string check
            drl.append("        $tx : Transaction(").append(rule.getFieldName());
            drl.append(" == null || ").append(rule.getFieldName()).append(" == \"\")\n");
        } else {
            // Simple condition
            drl.append("        $tx : Transaction(");
            drl.append(rule.getFieldName()).append(" != null, ");
            drl.append(rule.getFieldName()).append(" ").append(rule.getOperator()).append(" ");
            drl.append(formatValue(rule.getFieldName(), rule.getThresholdValue()));
            drl.append(")\n");
        }

        drl.append("    then\n");
        drl.append("        $tx.addTag(\"").append(rule.getTag()).append("\");\n");
        drl.append("        log.debug(\"[traceId={}] Rule '").append(rule.getRuleName());
        drl.append("' fired for transaction: {}\", traceId, $tx.getTransactionId());\n");
        drl.append("end");

        return drl.toString();
    }

    /**
     * Format value based on field type
     */
    private String formatValue(String fieldName, String value) {
        if (value == null || value.isEmpty()) {
            return "\"\"";
        }

        // Numeric fields
        if (fieldName.equals("amount") || fieldName.equals("riskScore")) {
            return value;
        }

        // String fields need quotes
        return "\"" + value + "\"";
    }

    /**
     * Inner class to represent a rule definition
     */
    private static class RuleDefinition {
        private String ruleName;
        private String fieldName;
        private String operator;
        private String thresholdValue;
        private String tag;
        private int priority = 100;
        private String conditionType = "simple";
        private String secondFieldName;
        private String secondOperator;
        private String secondThresholdValue;

        // Getters and setters
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public String getFieldName() { return fieldName; }
        public void setFieldName(String fieldName) { this.fieldName = fieldName; }
        public String getOperator() { return operator; }
        public void setOperator(String operator) { this.operator = operator; }
        public String getThresholdValue() { return thresholdValue; }
        public void setThresholdValue(String thresholdValue) { this.thresholdValue = thresholdValue; }
        public String getTag() { return tag; }
        public void setTag(String tag) { this.tag = tag; }
        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
        public String getConditionType() { return conditionType; }
        public void setConditionType(String conditionType) { this.conditionType = conditionType; }
        public String getSecondFieldName() { return secondFieldName; }
        public void setSecondFieldName(String secondFieldName) { this.secondFieldName = secondFieldName; }
        public String getSecondOperator() { return secondOperator; }
        public void setSecondOperator(String secondOperator) { this.secondOperator = secondOperator; }
        public String getSecondThresholdValue() { return secondThresholdValue; }
        public void setSecondThresholdValue(String secondThresholdValue) { this.secondThresholdValue = secondThresholdValue; }

        @Override
        public String toString() {
            return "RuleDefinition{" +
                    "ruleName='" + ruleName + '\'' +
                    ", fieldName='" + fieldName + '\'' +
                    ", operator='" + operator + '\'' +
                    ", thresholdValue='" + thresholdValue + '\'' +
                    ", tag='" + tag + '\'' +
                    ", priority=" + priority +
                    ", conditionType='" + conditionType + '\'' +
                    '}';
        }
    }
}
