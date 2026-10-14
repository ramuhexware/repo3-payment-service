package com.example.paymentservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ---- Repositories for Repo 3 Specific Tables ----
@Repository
interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByUserId(Long userId);
    List<PaymentTransaction> findByOrderId(Long orderId);
}

@Repository
interface PaymentInvoiceRepository extends JpaRepository<PaymentInvoice, Long> {
    Optional<PaymentInvoice> findByPaymentId(Long paymentId);
    Optional<PaymentInvoice> findByInvoiceNumber(String invoiceNumber);
}

// ---- Repositories for the 3 Common Tables ----
@Repository
interface UserRepository extends JpaRepository<AppUser, Long> {}

@Repository
interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByServiceName(String serviceName);
}

@Repository
interface NotificationRepository extends JpaRepository<SystemNotification, Long> {
    List<SystemNotification> findByUserId(Long userId);
}

// ---- Service orchestrating payment operations and common table integration ----
@Service
@Transactional
public class PaymentService {

    private final PaymentTransactionRepository paymentRepository;
    private final PaymentInvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationRepository notificationRepository;

    public PaymentService(PaymentTransactionRepository paymentRepository,
                          PaymentInvoiceRepository invoiceRepository,
                          UserRepository userRepository,
                          AuditLogRepository auditLogRepository,
                          NotificationRepository notificationRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Functional flow demonstrating:
     * 1. Querying Common Table 1 (app_users) to validate customer
     * 2. Inserting into Repo 3 Specific Table 1 (payment_transactions)
     * 3. Inserting into Repo 3 Specific Table 2 (payment_invoices)
     * 4. Inserting into Common Table 2 (audit_logs)
     * 5. Inserting into Common Table 3 (system_notifications)
     */
    public PaymentResult processPayment(Long orderId, Long userId, BigDecimal amount, String paymentMethod) {
        // 1. Validate user in Common Table: app_users
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " does not exist in app_users table"));

        // 2. Save in Specific Table 1: payment_transactions
        String txnRef = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        PaymentTransaction payment = paymentRepository.save(new PaymentTransaction(
                orderId, user.getId(), amount, paymentMethod, "SUCCESS", txnRef
        ));

        // 3. Save in Specific Table 2: payment_invoices
        BigDecimal tax = amount.multiply(new BigDecimal("0.08")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalWithTax = amount.add(tax);
        String invoiceNumber = "INV-" + System.currentTimeMillis();
        PaymentInvoice invoice = invoiceRepository.save(new PaymentInvoice(
                payment.getId(), invoiceNumber, tax, totalWithTax
        ));

        // 4. Save in Common Table 2: audit_logs
        AuditLog auditLog = auditLogRepository.save(new AuditLog(
                "payment-service",
                "PAYMENT_COMPLETED",
                "PAYMENT",
                payment.getId(),
                "Payment of $" + totalWithTax + " processed for user " + user.getUsername() + ", TxnRef: " + txnRef
        ));

        // 5. Save in Common Table 3: system_notifications
        SystemNotification notification = notificationRepository.save(new SystemNotification(
                user.getId(),
                "EMAIL",
                "Payment Receipt for Invoice #" + invoiceNumber,
                "Your payment of $" + totalWithTax + " was successful. Transaction reference: " + txnRef,
                "DELIVERED"
        ));

        return new PaymentResult(payment.getId(), txnRef, invoice.getInvoiceNumber(), totalWithTax,
                auditLog.getId(), notification.getId());
    }

    @Transactional(readOnly = true)
    public Optional<PaymentTransaction> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<PaymentInvoice> getInvoiceByPaymentId(Long paymentId) {
        return invoiceRepository.findByPaymentId(paymentId);
    }

    public record PaymentResult(
            Long paymentId,
            String transactionReference,
            String invoiceNumber,
            BigDecimal totalAmountPaid,
            Long auditLogId,
            Long notificationId
    ) {}
}
