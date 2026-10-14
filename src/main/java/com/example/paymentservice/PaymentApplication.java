package com.example.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@SpringBootApplication
public class PaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }
}

@RestController
@RequestMapping("/api/payments")
class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentService.PaymentResult> processPayment(@RequestBody Map<String, Object> request) {
        Long orderId = Long.valueOf(request.getOrDefault("orderId", "1").toString());
        Long userId = Long.valueOf(request.getOrDefault("userId", "1").toString());
        BigDecimal amount = new BigDecimal(request.getOrDefault("amount", "100.00").toString());
        String method = (String) request.getOrDefault("paymentMethod", "CREDIT_CARD");

        PaymentService.PaymentResult result = paymentService.processPayment(orderId, userId, amount, method);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentTransaction> getPayment(@PathVariable Long id) {
        return paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/receipt/{paymentId}")
    public ResponseEntity<PaymentInvoice> getInvoice(@PathVariable Long paymentId) {
        return paymentService.getInvoiceByPaymentId(paymentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
