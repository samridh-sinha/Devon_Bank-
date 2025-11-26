package com.devon.Bank.service;

import com.devon.Bank.dao.AccountStatementDao;
import com.devon.Bank.dto.AccountProcessingResult;
import com.devon.Bank.dto.StatementJobResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class StatementGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(StatementGenerationService.class);
    private final ExecutorService executor = Executors.newFixedThreadPool(8);

    private final AccountStatementDao accountStatementDao;

    public StatementGenerationService(AccountStatementDao accountStatementDao) {
        this.accountStatementDao = accountStatementDao;
    }

    public StatementJobResult generateForMonth(int year, int month) {

        long startTime = System.currentTimeMillis();
        logger.info("=== Starting monthly statement generation for {}/{} ===", month, year);

        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.plusMonths(1);

        Date startDate = java.sql.Date.valueOf(from);
        Date endDate = java.sql.Date.valueOf(to);

        long lastId = 0;
        int batchSize = 1000;

        int success = 0;
        int failed = 0;
        List<AccountProcessingResult> allResults = new ArrayList<>();

        while (true) {
            List<Long> batchIds = accountStatementDao.fetchAccountIdBatch(lastId, batchSize);

            if (batchIds.isEmpty()) {
                logger.info("No more accounts to process. Job complete.");
                break;
            }

            logger.info("Processing batch of {} accounts", batchIds.size());

            List<Future<AccountProcessingResult>> futures = new ArrayList<>();

            for (Long id : batchIds) {
                futures.add(executor.submit(() ->
                        accountStatementDao.processAccount(id, year, month, startDate, endDate)
                ));
            }

            for (Future<AccountProcessingResult> f : futures) {
                try {
                    AccountProcessingResult result = f.get();
                    allResults.add(result);

                    if (result.isSuccess()) success++;
                    else failed++;

                } catch (Exception e) {
                    logger.error("Unexpected error while processing account batch", e);
                    failed++;
                }
            }
            lastId = batchIds.get(batchIds.size() - 1);
        }

        long totalMillis = System.currentTimeMillis() - startTime;

        logger.info("=== Job finished. Success: {}, Failed: {}, Time: {} ms ===",
                success, failed, totalMillis);

        return new StatementJobResult(success, failed, allResults);
    }
}