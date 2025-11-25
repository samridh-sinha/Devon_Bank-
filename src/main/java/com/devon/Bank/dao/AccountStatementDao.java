package com.devon.Bank.dao;

import com.devon.Bank.dto.AccountProcessingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public class AccountStatementDao {

    private static final Logger logger = LoggerFactory.getLogger(AccountStatementDao.class);
    private final JdbcTemplate jdbc;

    public AccountStatementDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Fetch a batch of account IDs using keyset pagination.
     */
    public List<Long> fetchAccountIdBatch(long lastId, int batchSize) {

        String sql = """
            SELECT ID
            FROM ACCOUNT_DATA
            WHERE ID > ?
            ORDER BY ID
            FETCH FIRST ? ROWS ONLY
        """;

        logger.debug("Fetching batch: lastId={}, batchSize={}", lastId, batchSize);

        return jdbc.queryForList(sql, Long.class, lastId, batchSize);
    }

    // ---------------- Processing logic ----------------

    public AccountProcessingResult processAccount(Long accountId, int year, int month,
                                                  Date startDate, Date endDate) {
        long start = System.currentTimeMillis();
        logger.info("[Account {}] Processing started for {}/{}", accountId, month, year);

        try {
            BigDecimal opening = getOpeningBalance(accountId, startDate);
            Map<String, Object> totals = getMonthlyTotals(accountId, startDate, endDate);

            BigDecimal totalCredit = new BigDecimal(totals.get("TOTAL_CREDIT").toString());
            BigDecimal totalDebit = new BigDecimal(totals.get("TOTAL_DEBIT").toString());
            int txnCount = ((Number) totals.get("TXN_COUNT")).intValue();

            BigDecimal closing = opening.add(totalCredit).subtract(totalDebit);

            insertIntoAccountStatement(accountId, year, month,
                    opening, closing, totalCredit, totalDebit, txnCount);

            logger.info("[Account {}] SUCCESS ({} ms)",
                    accountId, (System.currentTimeMillis() - start));

            return AccountProcessingResult.success(
                    accountId, opening, closing, totalCredit, totalDebit, txnCount
            );

        } catch (Exception ex) {
            logger.error("[Account {}] FAILED: {}", accountId, ex.getMessage(), ex);
            return AccountProcessingResult.failure(accountId, ex.getMessage());
        }
    }

    private void insertIntoAccountStatement(Long accountId, int year, int month,
                                            BigDecimal opening, BigDecimal closing,
                                            BigDecimal totalCredit, BigDecimal totalDebit, int txnCount) {

        long generatedId = generateId();

        String insertSql = """
            INSERT INTO ACCOUNT_STATEMENT_DATA (
                ID, ACCOUNT_ID, YEAR_NUM, MONTH_NUM,
                OPENING_BALANCE, CLOSING_BALANCE,
                TOTAL_CREDIT, TOTAL_DEBIT, TXN_COUNT,
                GENERATED_AT
            ) VALUES (
               ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE
            )
        """;

        logger.debug("[Account {}] Inserting into ACCOUNT_STATEMENT", accountId);

        jdbc.update(insertSql, generatedId, accountId, year, month,
                opening, closing, totalCredit, totalDebit, txnCount);

        logger.debug("[Account {}] Insert completed", accountId);
    }

    private Map<String, Object> getMonthlyTotals(Long accountId, Date startDate, Date endDate) {

        String totalsSql = """
            SELECT 
                NVL(SUM(CASE WHEN TXN_TYPE = 'CREDIT' THEN AMOUNT ELSE 0 END), 0) AS TOTAL_CREDIT,
                NVL(SUM(CASE WHEN TXN_TYPE = 'DEBIT' THEN AMOUNT ELSE 0 END), 0) AS TOTAL_DEBIT,
                COUNT(*) AS TXN_COUNT
            FROM TRANSACTION_DATA
            WHERE ACCOUNT_ID = ?
              AND TXN_TIMESTAMP >= ?
              AND TXN_TIMESTAMP < ?
        """;

        logger.debug("[Account {}] Running totals query", accountId);

        return jdbc.queryForMap(totalsSql, accountId, startDate, endDate);
    }

    private BigDecimal getOpeningBalance(Long accountId, Date startDate) {

        String openingSql = """
            SELECT NVL(SUM(
                CASE 
                  WHEN TXN_TYPE = 'CREDIT' THEN AMOUNT 
                  WHEN TXN_TYPE = 'DEBIT'  THEN -AMOUNT
                  ELSE 0 END
            ), 0)
            FROM TRANSACTION_DATA
            WHERE ACCOUNT_ID = ?
              AND TXN_TIMESTAMP < ?
        """;

        logger.debug("[Account {}] Running opening balance query", accountId);

        return jdbc.queryForObject(openingSql, BigDecimal.class, accountId, startDate);
    }

    private long generateId() {
        return 100_000_000_000L + (long) (Math.random() * 900_000_000_000L);
    }
}