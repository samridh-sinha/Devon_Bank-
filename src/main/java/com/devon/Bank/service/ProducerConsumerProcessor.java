package com.devon.Bank.service;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ProducerConsumerProcessor<T> {
    private final BlockingQueue<List<T>> queue;
    private final ExecutorService workers;
    private final int consumerCount;

    public ProducerConsumerProcessor(int queueCapacity, int consumerCount, String threadNamePrefix) {
        this.queue = new ArrayBlockingQueue<>(queueCapacity);
        this.consumerCount = consumerCount;
        this.workers = Executors.newFixedThreadPool(consumerCount, r -> new Thread(r, threadNamePrefix + "-worker"));
    }

    public void startConsumers(Consumer<List<T>> batchProcessor) {
        for (int i = 0; i < consumerCount; i++) {
            workers.submit(() -> {
                try {
                    while (true) {
                        List<T> batch = queue.take();
                        // poison pill: empty list
                        if (batch == null || batch.isEmpty()) break;
                        batchProcessor.accept(batch);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    public void produce(List<T> batch) throws InterruptedException {
        queue.put(batch);
    }

    public void stopAndAwait(long timeoutMinutes) throws InterruptedException {
        // send poison pills
        for (int i = 0; i < consumerCount; i++) queue.put(List.of());
        workers.shutdown();
        workers.awaitTermination(timeoutMinutes, TimeUnit.MINUTES);
    }
}