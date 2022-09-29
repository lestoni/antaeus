package io.pleo.antaeus.core.services

import mu.KotlinLogging
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus

private val logger = KotlinLogging.logger {}

/**
 * Billing Logic Approach
 *  
 * 
 **/
class BillingService(
    private val paymentProvider: PaymentProvider,
    private val dal: AntaeusDal
) {
    
    fun start() {
        // Retrieve Pending invoices
        val invoices = dal.fetchInvoices()
        val pendingInvoices = invoices.filter { it.status == InvoiceStatus.PENDING }

        // Charge Customer
        pendingInvoices.forEach {
            paymentProvider.charge(it)
            dal.updateInvoiceStatus(it.id, InvoiceStatus.PAID)
        }

        val invoices2 = dal.fetchInvoices()
        val pendingInvoices2 = invoices2.filter { it.status == InvoiceStatus.PENDING }
        println("Any Pending invoices: ${pendingInvoices2.size}")
    }

}
