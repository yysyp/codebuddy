package com.transaction.tagging.datapanel.sink;

import com.transaction.tagging.common.entity.Transaction;
import com.transaction.tagging.datapanel.config.DataPanelConfig;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * File-based sink for writing tagged transaction data (placeholder).
 */
public class FileTransactionSink implements SinkFunction<Transaction> {

    private static final long serialVersionUID = 1L;

    public FileTransactionSink(DataPanelConfig config) {
        // File sink configuration
    }

    @Override
    public void invoke(Transaction transaction, Context context) throws Exception {
        // Placeholder - implement file writing logic
    }
}
