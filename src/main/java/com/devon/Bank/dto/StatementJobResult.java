package com.devon.Bank.dto;

import java.util.List;

public class StatementJobResult {

    private int successCount;
    private int failedCount;
    private List<AccountProcessingResult> details;

    public StatementJobResult() {}

    public StatementJobResult(int successCount, int failedCount, List<AccountProcessingResult> details) {
        this.successCount = successCount;
        this.failedCount = failedCount;
        this.details = details;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public List<AccountProcessingResult> getDetails() {
        return details;
    }

    public void setDetails(List<AccountProcessingResult> details) {
        this.details = details;
    }
}
