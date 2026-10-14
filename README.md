# Repo 3: Payment Service (`repo3-payment-service`)

Spring Boot Microservice managing **2 repo-specific tables** and accessing the **3 shared common tables** in PostgreSQL:

## Tables
- **2 Repo-Specific Tables** (used only in this repository):
  1. `payment_transactions` (Transaction reference, payment method, amount, status)
  2. `payment_invoices` (Invoice number, tax amount, total amount, issue timestamp)
- **3 Shared Common Tables** (used across all 3 microservices):
  1. `app_users` (Validates payer identity before charging)
  2. `audit_logs` (Records `PAYMENT_COMPLETED` audit events)
  3. `system_notifications` (Sends payment receipt notification)

## Port
`8083`

## Functional Flow
1. Receives payment charge request with amount and payment method.
2. Validates user existence against common `app_users` table.
3. Inserts transaction into `payment_transactions`.
4. Calculates sales tax and generates invoice in `payment_invoices`.
5. Logs audit entry into common `audit_logs` table (`serviceName="payment-service"`).
6. Dispatches receipt notification into common `system_notifications` table.

## Running the Application
```bash
mvn spring-boot:run
```

## REST Endpoints
- `POST /api/payments/process` - Process payment & invoice (updates payment_transactions, payment_invoices, audit_logs, system_notifications)
- `GET  /api/payments/{id}` - Fetch payment details by ID
- `GET  /api/payments/receipt/{paymentId}` - Fetch invoice and tax details by payment ID
