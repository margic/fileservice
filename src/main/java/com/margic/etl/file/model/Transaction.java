package com.margic.etl.file.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Created by paulcrofts on 6/4/16.
 * lombok data class.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    /**
     * Trace id for this transaction.
     */
    private String traceId;
    /**
     * Primary account number.
     */
    private String pan;
    /**
     * Merchant identifier.
     */
    private String merchant;
    /**
     * Transaction amount.
     */
    private BigDecimal amount;
    /**
     * Transaction currentcy code.
     */
    private String currencyCode;

}
