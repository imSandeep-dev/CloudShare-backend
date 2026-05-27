package com.sandeep.controller;

import com.sandeep.dto.PaymentDto;
import com.sandeep.dto.PaymentTransactionDto;
import com.sandeep.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<?> getUserTransactions(){
        List<PaymentTransactionDto> response = paymentService.getUserTransactions();
        return ResponseEntity.ok(response);
    }

}
