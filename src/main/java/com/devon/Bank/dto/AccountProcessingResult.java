package com.devon.Bank.dto;




import java.math.BigDecimal;

public class AccountProcessingResult {

    // Getters and setters
    private Long accountId;
    private boolean success;
    private String error;

    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private BigDecimal totalCredit;
    private BigDecimal totalDebit;
    private int transactionCount;

    public AccountProcessingResult() {}

    public AccountProcessingResult(Long accountId, boolean success, String error,
                                   BigDecimal openingBalance, BigDecimal closingBalance,
                                   BigDecimal totalCredit, BigDecimal totalDebit,
                                   int transactionCount) {
        this.accountId = accountId;
        this.success = success;
        this.error = error;
        this.openingBalance = openingBalance;
        this.closingBalance = closingBalance;
        this.totalCredit = totalCredit;
        this.totalDebit = totalDebit;
        this.transactionCount = transactionCount;
    }

    // Factory methods
    public static AccountProcessingResult success(Long id, BigDecimal open, BigDecimal close,
                                                  BigDecimal credit, BigDecimal debit, int count) {
        return new AccountProcessingResult(id, true, null, open, close, credit, debit, count);
    }

    public static AccountProcessingResult failure(Long id, String error) {
        return new AccountProcessingResult(id, false, error, null, null, null, null, 0);
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }

    public void setClosingBalance(BigDecimal closingBalance) {
        this.closingBalance = closingBalance;
    }

    public void setTotalCredit(BigDecimal totalCredit) {
        this.totalCredit = totalCredit;
    }

    public void setTotalDebit(BigDecimal totalDebit) {
        this.totalDebit = totalDebit;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }


    public Long getAccountId() {
        return accountId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public BigDecimal getClosingBalance() {
        return closingBalance;
    }

    public BigDecimal getTotalCredit() {
        return totalCredit;
    }

    public BigDecimal getTotalDebit() {
        return totalDebit;
    }

    public int getTransactionCount() {
        return transactionCount;
    }
}
