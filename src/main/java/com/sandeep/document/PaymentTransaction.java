package com.sandeep.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "payment_transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
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
