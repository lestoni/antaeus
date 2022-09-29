package io.pleo.antaeus.models

enum class InvoiceStatus {
    PENDING,
    PAID,
    FAILED // TODO: In real world scenario we would need to retry charging the customer
}
