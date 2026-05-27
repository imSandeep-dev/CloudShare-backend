package com.sandeep.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentTransactionDto {

    private String Id;
    private String clerkId;
    private String orderId;
    private String paymentId;
    private String planId;
    private Integer amount;
    private String currency;
    private Integer creditsAdded;
    private String status;
    private LocalDateTime transactionDate;
    private String userName;
    private String userEmail;

}
