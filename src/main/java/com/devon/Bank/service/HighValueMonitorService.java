package com.devon.Bank.service;


import com.devon.Bank.dao.TransactionDao;
import com.devon.Bank.model.Transaction;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class HighValueMonitorService {
    private static final Logger logger = LoggerFactory.getLogger(HighValueMonitorService.class);

    private final TransactionDao dao;
    private final JdbcTemplate jdbc;
    private final BigDecimal threshold;
    private final int batchSize;
    private final int fetchSize;
    private final ProducerConsumerProcessor<Transaction> processor;
    private final Timer timer;

    public HighValueMonitorService(TransactionDao dao,
                                   JdbcTemplate jdbc,
                                   @Value("${risk.highValueThreshold}") BigDecimal threshold,
                                   @Value("${job.batchSize}") int batchSize,
                                   @Value("${spring.jdbc.fetch-size}") int fetchSize,
                                   @Value("${job.queueCapacity}") int queueCapacity,
                                   @Value("${job.consumerThreads}") int consumerThreads,
                                   MeterRegistry registry) {
        this.dao = dao;
        this.jdbc = jdbc;
        this.threshold = threshold;
        this.batchSize = batchSize;
        this.fetchSize = fetchSize;
        this.processor = new ProducerConsumerProcessor<>(queueCapacity, consumerThreads, "hv");
        this.timer = registry.timer("highValue.monitor.time");
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void scheduledRun() {
        logger.info("Scheduled high-value monitor triggered");
        runForDate(LocalDate.now().minusDays(1));
    }

    public void runForDate(LocalDate date) {
        logger.info("Starting HighValueMonitor for date {}", date);
        timer.record(() -> doRun(date));
    }

    private void doRun(LocalDate date) {
        AtomicLong processed = new AtomicLong();
        AtomicLong highCount = new AtomicLong();

        processor.startConsumers(batch -> {
            long start = System.currentTimeMillis();
            try {
                long batchSizeLocal = batch.size();
                processed.addAndGet(batchSizeLocal);

                batch.stream()
                        .filter(t -> t.getAmount() != null && t.getAmount().compareTo(threshold) > 0)
                        .forEach(t -> {
                            insertHighValue(t);
                            highCount.incrementAndGet();
                        });
                long timeMs = System.currentTimeMillis() - start;
                logger.info("Processed batch of size {} in {} ms on thread {}", batchSizeLocal, timeMs, Thread.currentThread().getName());
            } catch (Exception e) {
                logger.error("Error processing batch", e);
            }
        });
        try {
            dao.streamTransactionsByDate(date, fetchSize, batchSize, batch -> {
                try {
                    processor.produce(batch);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            });
            processor.stopAndAwait(5);
            logger.info("HighValueMonitor finished. processed={}, highValueCount={}", processed.get(), highCount.get());
        } catch (SQLException e) {
            logger.error("SQL error during streaming", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while waiting for consumers", e);
        }
    }

    private void insertHighValue(Transaction t) {

        String sql = "INSERT INTO HIGH_VALUE_TXN_DATA " +
                     "(ID, TXN_ID, ACCOUNT_ID, AMOUNT, TXN_TIMESTAMP, REASON, CREATED_AT) " +
                     "VALUES (?, ?, ?, ?, ?, ?, SYSDATE)";


        jdbc.update(sql,
                generateId(),
                t.getId(),
                t.getAccountId(),
                t.getAmount(),
                Timestamp.valueOf(t.getTxnTimeStamp()),
                "AMOUNT_ABOVE_THRESHOLD"
        );
    }

    private long generateId() {
        return 100_000_000_000L + (long) (Math.random() * 900_000_000_000L);
    }
}
