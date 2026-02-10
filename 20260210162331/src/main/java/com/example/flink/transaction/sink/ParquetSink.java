package com.example.flink.transaction.sink;

import com.example.flink.transaction.model.Transaction;
import com.example.flink.transaction.util.TraceIdUtil;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Parquet Sink for writing transactions to CSV format (Parquet alternative)
 * Uses Flink's StreamingFileSink for fault-tolerant output
 * Thread-safe implementation with proper resource management
 *
 * Note: For true Parquet output, consider using Flink's Table & SQL API with Parquet format
 * or use libraries like Apache Arrow for Parquet serialization.
 *
 * This implementation writes to CSV format which can be easily converted to Parquet later.
 */
public class ParquetSink {

    private static final Logger LOG = LoggerFactory.getLogger(ParquetSink.class);

    private final String outputPath;
    private transient AtomicInteger recordsWritten;

    /**
     * Constructor
     *
     * @param outputPath output directory path for files
     */
    public ParquetSink(String outputPath) {
        this.outputPath = outputPath;
    }

    /**
     * Create a StreamingFileSink for Flink
     *
     * @return StreamingFileSink configured for transaction output
     */
    public static StreamingFileSink<String> createStreamingFileSink(String outputPath) {
        final Path path = new Path(outputPath);

        // Define rolling policy
        DefaultRollingPolicy<String, String> rollingPolicy = DefaultRollingPolicy
                .builder()
                .withRolloverInterval(TimeUnit.MINUTES.toMillis(5))
                .withInactivityInterval(TimeUnit.MINUTES.toMillis(1))
                .withMaxPartSize(128 * 1024 * 1024) // 128 MB
                .build();

        // Create streaming file sink
        return StreamingFileSink
                .forRowFormat(path, new SimpleStringEncoder<String>("UTF-8"))
                .withRollingPolicy(rollingPolicy)
                .build();
    }

    public static String convertTransactionToString(Transaction transaction) {
        if (transaction == null) {
            return "";
        }

        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%d,\"%s\",%s",
                transaction.getTransactionId(),
                transaction.getCustomerId(),
                transaction.getSourceAccount(),
                transaction.getDestinationAccount(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getTransactionType(),
                transaction.getTimestamp() != null ? transaction.getTimestamp().toEpochMilli() : 0,
                transaction.getLocationCountry(),
                transaction.getMerchantCategory(),
                transaction.getIpAddress(),
                transaction.getDeviceId(),
                transaction.getStatus(),
                transaction.getRiskScore(),
                transaction.getTagsAsString().replace("\"", "'"), // Escape quotes
                transaction.getTraceId()
        );
    }
}
