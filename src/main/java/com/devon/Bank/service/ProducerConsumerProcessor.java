package com.devon.Bank.service;

import com.devon.Bank.model.Transaction;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ProducerConsumerProcessor {
    private final BlockingQueue<List<Transaction>> queue;
    private final ExecutorService workers;
    private final int consumerCount;

    public ProducerConsumerProcessor(int queueCapacity, int consumerCount, String threadNamePrefix) {
        this.queue = new ArrayBlockingQueue<>(queueCapacity);
        this.consumerCount = consumerCount;
        this.workers = Executors.newFixedThreadPool(consumerCount, r -> new Thread(r, threadNamePrefix + "-worker"));
    }

    public void startConsumers(Consumer<List<Transaction>> batchProcessor) {
        for (int i = 0; i < consumerCount; i++) {
            workers.submit(() -> {
                try {
                    while (true) {
                        //Gives the first batch from the queue
                        List<Transaction> batch = queue.take();
                        // poison pill: empty list
                        if (batch.isEmpty()) break;
                        batchProcessor.accept(batch);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    public void produce(List<Transaction> batch) throws InterruptedException {
        queue.put(batch);
    }

    public void stopAndAwait(long timeoutMinutes) throws InterruptedException {
        for (int i = 0; i < consumerCount; i++) {
            queue.put(List.of());
        }
        workers.shutdown();
        workers.awaitTermination(timeoutMinutes, TimeUnit.MINUTES);
    }
}