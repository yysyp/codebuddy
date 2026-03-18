package com.transaction.tagging.datapanel.source;

import com.transaction.tagging.common.entity.Transaction;
import com.transaction.tagging.datapanel.config.DataPanelConfig;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

/**
 * File-based source for reading transaction data (placeholder).
 */
public class FileTransactionSource implements SourceFunction<Transaction> {

    private static final long serialVersionUID = 1L;

    public FileTransactionSource(DataPanelConfig config) {
        // File source configuration
    }

    @Override
    public void run(SourceContext<Transaction> ctx) throws Exception {
        // Placeholder - implement file reading logic
    }

    @Override
    public void cancel() {
        // No-op
    }
}
