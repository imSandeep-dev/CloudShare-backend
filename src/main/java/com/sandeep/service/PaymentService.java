package com.sandeep.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.sandeep.document.PaymentTransaction;
import com.sandeep.document.ProfileDocument;
import com.sandeep.dto.PaymentDto;
import com.sandeep.dto.PaymentTransactionDto;
import com.sandeep.dto.PaymentVerificationDto;
import com.sandeep.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserCreditsService userCreditsService;
    private final ProfileService profileService;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public PaymentDto createOrder(PaymentDto paymentDto){
        try {
            ProfileDocument currentProfile=profileService.getCurrentProfile();
            String clerkId = currentProfile.getClerkId();

            RazorpayClient razorpayClient=new RazorpayClient(razorpayKeyId,razorpayKeySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount",paymentDto.getAmount());
            orderRequest.put("currency",paymentDto.getCurrency());
            orderRequest.put("receipt","order_"+System.currentTimeMillis());

            Order order = razorpayClient.orders.create(orderRequest);
            String orderId=order.get("id");

            PaymentTransaction paymentTransaction=PaymentTransaction.builder()
                    .orderId(orderId)
                    .clerkId(clerkId)
                    .currency(paymentDto.getCurrency())
                    .amount(paymentDto.getAmount())
                    .status("PENDING")
                    .planId(paymentDto.getPlanId())
                    .transactionDate(LocalDateTime.now())
                    .userEmail(currentProfile.getEmail())
                    .userName(currentProfile.getFirstName()+" "+currentProfile.getLastName())
                    .build();
            paymentTransactionRepository.save(paymentTransaction);
            return PaymentDto.builder()
                    .orderId(orderId)
                    .success(true)
                    .message("Order created successfully")
                    .build();

        } catch (Exception e) {
            return PaymentDto.builder()
                    .success(false)
                    .message("Error creating order:"+e.getMessage())
                    .build();
        }
    }

    public PaymentDto verifyPayment(PaymentVerificationDto request){
        try {
            ProfileDocument currentProfile=profileService.getCurrentProfile();
            String clerkId=currentProfile.getClerkId();
            String data = request.getRazorpay_order_id() + "|" + request.getRazorpay_payment_id();
            String generatedSignature=generateHmacSha256Signature(data,razorpayKeySecret);
            if(!generatedSignature.equals(request.getRazorpay_signature())){
                updateTransactionStatus(request.getRazorpay_order_id(),"Failed",request.getRazorpay_payment_id(),null);
                return PaymentDto.builder()
                        .success(false)
                        .message("Payment signature verification failed")
                        .build();
            }
            int creditsToAdd = 0;
            String plan = "BASIC";
            switch (request.getPlanId().toUpperCase()){
                case "PREMIUM":
                    plan="PREMIUM";
                    creditsToAdd=500;
                    break;
                case "ULTIMATE":
                    plan="ULTIMATE";
                    creditsToAdd=5000;
                    break;
            }
            if(creditsToAdd>0){
                userCreditsService.addCredits(clerkId,creditsToAdd,plan);
                updateTransactionStatus(request.getRazorpay_order_id(),"SUCCESS",request.getRazorpay_payment_id(),creditsToAdd);
                return PaymentDto.builder()
                        .success(true)
                        .message("Payment verified and credits added successfully")
                        .build();
            }else{
                updateTransactionStatus(request.getRazorpay_order_id(),"FAILED",request.getRazorpay_payment_id(),null);
                return PaymentDto.builder()
                        .success(false)
                        .message("Invalid plan selected")
                        .build();
            }
        } catch (Exception e) {
            try {
                updateTransactionStatus(request.getRazorpay_order_id(),"ERROR",request.getRazorpay_payment_id(),null);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return PaymentDto.builder()
                    .success(false)
                    .message("Error verifying payment: "+e.getMessage())
                    .build();
        }
    }

    private void updateTransactionStatus(String razorpayOrderId, String status, String razorpayPaymentId, Integer credits) {
        paymentTransactionRepository.findAll().stream()
                .filter(t->t.getOrderId()!=null && t.getOrderId().equals(razorpayOrderId))
                .findFirst()
                .map(transaction -> {
                    transaction.setPaymentId(razorpayPaymentId);
                    transaction.setStatus(status);
                    if(credits!=null){
                        transaction.setCreditsAdded(credits);
                    }
                    return paymentTransactionRepository.save(transaction);
                })
                .orElse(null);
    }

    private String generateHmacSha256Signature(String data, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKey secretKey=new SecretKeySpec(secret.getBytes(),"HmacSHA256");
        Mac mac=Mac.getInstance("HmacSHA256");
        mac.init(secretKey);
        byte[] hmacData = mac.doFinal(data.getBytes());
        return toHexString(hmacData);
    }

    private String toHexString(byte[] bytes){
        Formatter formatter=new Formatter();
        for(byte b:bytes){
            formatter.format("%02x",b);
        }
        String result =formatter.toString();
        formatter.close();
        return result;
    }

    public List<PaymentTransactionDto> getUserTransactions(){
        ProfileDocument currentProfile = profileService.getCurrentProfile();
        String clerkId = currentProfile.getClerkId();
        List<PaymentTransaction> transactions = paymentTransactionRepository.findByClerkIdAndStatusOrderByTransactionDateDesc(clerkId,"SUCCESS");
        List<PaymentTransactionDto> response=new ArrayList<>();
        for(PaymentTransaction p:transactions){
            PaymentTransactionDto paymentDto=PaymentTransactionDto.builder()
                    .Id(p.getId())
                    .clerkId(p.getClerkId())
                    .paymentId(p.getPaymentId())
                    .orderId(p.getOrderId())
                    .planId(p.getPlanId())
                    .amount(p.getAmount()/100)
                    .creditsAdded(p.getCreditsAdded())
                    .currency(p.getCurrency())
                    .status(p.getStatus())
                    .transactionDate(p.getTransactionDate())
                    .userName(p.getUserName())
                    .userEmail(p.getUserEmail())
                    .build();
            response.add(paymentDto);
        }
        return response;
    }

}
