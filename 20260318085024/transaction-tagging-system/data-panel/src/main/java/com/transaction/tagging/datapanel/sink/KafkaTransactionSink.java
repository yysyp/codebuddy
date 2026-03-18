package com.transaction.tagging.datapanel.sink;

import com.transaction.tagging.common.entity.Transaction;
import com.transaction.tagging.datapanel.config.DataPanelConfig;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * Kafka sink for writing tagged transaction data (placeholder).
 */
public class KafkaTransactionSink implements SinkFunction<Transaction> {

    private static final long serialVersionUID = 1L;

    public KafkaTransactionSink(DataPanelConfig config) {
        // Kafka sink configuration
    }

    @Override
    public void invoke(Transaction transaction, Context context) throws Exception {
        // Placeholder - in production, use FlinkKafkaProducer
    }
}
