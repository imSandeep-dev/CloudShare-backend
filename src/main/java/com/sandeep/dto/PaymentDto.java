package com.sandeep.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PaymentDto {

    private String planId;
    private Integer amount;
    private String currency;
    private Integer credits;
    private String orderId;
    private String message;
    private Boolean success;

}
