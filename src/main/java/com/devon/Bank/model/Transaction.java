package com.devon.Bank.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class Transaction {
    private long id;
    private long accountId;
    private BigDecimal amount;
    private String txnType;
    private LocalDateTime txnTimeStamp;
    private String channel;
    private String status;
    private String description;

}
