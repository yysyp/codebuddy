package com.transaction.tagging.datapanel.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.transaction.tagging.common.entity.Transaction;
import com.transaction.tagging.datapanel.config.DataPanelConfig;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.Properties;

/**
 * Kafka source for reading transaction data.
 */
public class KafkaTransactionSource implements SourceFunction<Transaction> {

    private static final long serialVersionUID = 1L;

    private final DataPanelConfig config;

    public KafkaTransactionSource(DataPanelConfig config) {
        this.config = config;
    }

    @Override
    public void run(SourceContext<Transaction> ctx) throws Exception {
        // This is a placeholder - actual Kafka consumer is created via FlinkKafkaConsumer
    }

    @Override
    public void cancel() {
        // No-op
    }

    /**
     * Create FlinkKafkaConsumer for Kafka source.
     */
    public static FlinkKafkaConsumer<String> createKafkaConsumer(DataPanelConfig config) {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafkaBootstrapServers());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, config.getKafkaGroupId());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new FlinkKafkaConsumer<>(
                config.getKafkaTopic(),
                new SimpleStringSchema(),
                properties
        );
    }
}
