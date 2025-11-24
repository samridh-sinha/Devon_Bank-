package com.devon.Bank.dao;

import com.devon.Bank.model.Transaction;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Repository
public class TransactionDao {

    private final DataSource dataSource;

    public TransactionDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void streamTransactionsByDate(LocalDate date, int fetchSize, int batchSize, Consumer<List<Transaction>> batchConsumer) throws SQLException {
        String sql = "SELECT ID, ACCOUNT_ID, AMOUNT, TXN_TYPE, TXN_TIMESTAMP, CHANNEL, STATUS, DESCRIPTION " +
                     "FROM \"TRANSACTION_DATA\" WHERE TRUNC(TXN_TIMESTAMP) = ? ORDER BY ID";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {

            statement.setFetchSize(fetchSize);
            statement.setDate(1, Date.valueOf(date));

            try (ResultSet rs = statement.executeQuery()) {
                List<Transaction> batch = new ArrayList<>(batchSize);
                while (rs.next()) {
                    Transaction t = mapRow(rs);
                    batch.add(t);
                    if (batch.size() >= batchSize) {
                        batchConsumer.accept(new ArrayList<>(batch));
                        batch.clear();
                    }
                }
                if (!batch.isEmpty()) batchConsumer.accept(batch);
            }
        }
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getLong("ID"));
        transaction.setAccountId(rs.getLong("ACCOUNT_ID"));
        transaction.setAmount(rs.getBigDecimal("AMOUNT"));
        Timestamp ts = rs.getTimestamp("TXN_TIMESTAMP");
        if (ts != null) transaction.setTxnTimeStamp(ts.toLocalDateTime());
        transaction.setTxnType(rs.getString("TXN_TYPE"));
        transaction.setChannel(rs.getString("CHANNEL"));
        transaction.setStatus(rs.getString("STATUS"));
        transaction.setDescription(rs.getString("DESCRIPTION"));
        return transaction;
    }


}
