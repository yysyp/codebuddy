package com.transaction.tagging.datapanel.sink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.transaction.tagging.common.entity.Transaction;
import com.transaction.tagging.datapanel.config.DataPanelConfig;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sink function for outputting tagged transactions.
 * Supports console, Kafka, and file outputs.
 */
public class TaggedTransactionSink implements SinkFunction<Transaction> {

    private static final Logger LOG = LoggerFactory.getLogger(TaggedTransactionSink.class);
    private static final long serialVersionUID = 1L;

    private final String sinkType;
    private final ObjectMapper objectMapper;
    private transient long count;

    public TaggedTransactionSink(DataPanelConfig config) {
        this.sinkType = config.getSinkType();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void invoke(Transaction transaction, Context context) throws Exception {
        count++;
        
        switch (sinkType.toLowerCase()) {
            case "console" -> outputToConsole(transaction);
            case "kafka" -> outputToKafka(transaction);
            case "file" -> outputToFile(transaction);
            default -> outputToConsole(transaction);
        }
        
        if (count % 1000 == 0) {
            LOG.info("Processed {} tagged transactions", count);
        }
    }

    private void outputToConsole(Transaction transaction) {
        try {
            String json = objectMapper.writeValueAsString(transaction);
            System.out.println("Tagged Transaction: " + json);
        } catch (Exception e) {
            LOG.error("Error serializing transaction", e);
        }
    }

    private void outputToKafka(Transaction transaction) {
        // Placeholder for Kafka output
        // In production, use FlinkKafkaProducer
        LOG.debug("Output to Kafka: {}", transaction.getTransactionId());
    }

    private void outputToFile(Transaction transaction) {
        // Placeholder for file output
        LOG.debug("Output to file: {}", transaction.getTransactionId());
    }

    /**
     * Create sink based on configuration.
     */
    public static SinkFunction<Transaction> create(DataPanelConfig config) {
        return switch (config.getSinkType().toLowerCase()) {
            case "console" -> new TaggedTransactionSink(config);
            case "kafka" -> new KafkaTransactionSink(config);
            case "file" -> new FileTransactionSink(config);
            default -> new TaggedTransactionSink(config);
        };
    }
}
