package com.devon.Bank.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HighValueTxn {

    private long id;
    private long txnId;
    private long accountId;
    private BigDecimal amount;
    private LocalDateTime txnTimeStamp;
    private String reason;
}
