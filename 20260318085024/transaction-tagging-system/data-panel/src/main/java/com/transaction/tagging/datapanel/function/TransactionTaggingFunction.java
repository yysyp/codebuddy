package com.transaction.tagging.datapanel.function;

import com.transaction.tagging.common.entity.RuleMetadata;
import com.transaction.tagging.common.entity.Transaction;
import com.transaction.tagging.datapanel.config.DataPanelConfig;
import com.transaction.tagging.datapanel.rule.DroolsRuleEngine;
import com.transaction.tagging.datapanel.rule.RuleFetcher;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Flink ProcessFunction for applying tagging rules to transactions.
 * This function:
 * 1. Fetches rules from Control Panel
 * 2. Applies rules using Drools engine
 * 3. Outputs tagged transactions
 */
public class TransactionTaggingFunction extends ProcessFunction<Transaction, Transaction> 
        implements CheckpointedFunction {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionTaggingFunction.class);

    private final DataPanelConfig config;
    
    // Transient fields (not serialized)
    private transient RuleFetcher ruleFetcher;
    private transient DroolsRuleEngine ruleEngine;
    
    // State for checkpointing
    private transient ListState<String> rulesVersionState;

    public TransactionTaggingFunction(DataPanelConfig config) {
        this.config = config;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        
        LOG.info("Opening TransactionTaggingFunction, config: {}", config);
        
        // Initialize rule fetcher and engine
        this.ruleFetcher = new RuleFetcher(config);
        this.ruleEngine = new DroolsRuleEngine(config);
        
        // Start rule fetcher
        ruleFetcher.start();
        
        // Initial rule load
        updateRules();
    }

    @Override
    public void close() throws Exception {
        LOG.info("Closing TransactionTaggingFunction");
        
        if (ruleFetcher != null) {
            ruleFetcher.stop();
        }
        if (ruleEngine != null) {
            ruleEngine.dispose();
        }
        
        super.close();
    }

    @Override
    public void processElement(Transaction transaction, 
                               Context ctx, 
                               Collector<Transaction> out) throws Exception {
        
        // Check for rule updates
        List<RuleMetadata> rules = ruleFetcher.getCachedRules();
        if (!rules.isEmpty() && 
            (ruleEngine.getCurrentRulesVersion() == null || 
             !ruleEngine.hasRules())) {
            updateRules();
        }
        
        // Apply rules to transaction
        try {
            ruleEngine.applyRules(transaction);
            
            LOG.debug("Transaction {} tagged with {} tags", 
                    transaction.getTransactionId(), 
                    transaction.getTags() != null ? transaction.getTags().size() : 0);
            
            // Output tagged transaction
            out.collect(transaction);
        } catch (Exception e) {
            LOG.error("Error processing transaction: {}", transaction.getTransactionId(), e);
            // Output original transaction without tags
            out.collect(transaction);
        }
    }

    private void updateRules() {
        List<RuleMetadata> rules = ruleFetcher.getCachedRules();
        if (!rules.isEmpty()) {
            ruleEngine.updateRules(rules);
            LOG.info("Rules updated, count: {}", rules.size());
        }
    }

    // Checkpoint methods for fault tolerance

    @Override
    public void snapshotState(FunctionSnapshotContext context) throws Exception {
        // Clear and update state
        rulesVersionState.clear();
        if (ruleEngine.getCurrentRulesVersion() != null) {
            rulesVersionState.add(ruleEngine.getCurrentRulesVersion());
        }
    }

    @Override
    public void initializeState(FunctionInitializationContext context) throws Exception {
        // Initialize state
        ListStateDescriptor<String> descriptor = new ListStateDescriptor<>(
                "rules-version-state",
                TypeInformation.of(String.class)
        );
        rulesVersionState = context.getOperatorStateStore().getListState(descriptor);
    }
}
