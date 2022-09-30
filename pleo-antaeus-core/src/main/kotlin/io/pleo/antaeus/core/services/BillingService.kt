package io.pleo.antaeus.core.services

import mu.KotlinLogging
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.core.exceptions.NetworkException
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.runBlocking

private val logger = KotlinLogging.logger {}

class BillingService(
    private val invoiceService: InvoiceService,
    private val paymentProvider: PaymentProvider
) {
    
    fun run() {
        runBlocking {
            logger.info { "Billing Service has Started" }
            processPendingInvoices()
            logger.info { "Billing Service Successfully completed" }
        }
    }

    suspend private fun processPendingInvoices() {
        val pendingInvoices = invoiceService.fetchPendingInvoices()

        return pendingInvoices
            .asFlow()
            .flatMapMerge(concurrency = 10) {
                flow {
                    payTheInvoice(it)
                    emit(it)
                }
            }
            .collect()
    }

    private fun payTheInvoice(invoice: Invoice) {
        // FIXME: Logs below in real world will bloat logs, better to use
        // Minimally in calling parent func 
        logger.info { "Charging Customer for Invoice#${invoice.id}" }
        // We expect to have only pending invoices other break early
        if(invoice.status != InvoiceStatus.PENDING) {
            logger.info { "Expecting a PENDING invoice for Invoice#${invoice.id} but received ${invoice.status}" }
            return
        }

        try {

            val paymentSucceeded = paymentProvider.charge(invoice)

            if (paymentSucceeded) {
                invoiceService.setInvoiceAsPaid(invoice.id)
            } else {
                determineSettlementRetries(invoice, "Payment Method Failed!")
            }

        } catch (e: Exception) {
            // It doesn't make sense to set status of an invoice as FAILED yet
            // Maybe there was a network timeout issue, hence makes sense to retry again
            determineSettlementRetries(invoice, e.toString())

            // FIXME: Errors should be mapped correctly to be user-friendly
            // For now we will only log
            when(e) {
                is NetworkException -> {
                    logger.error(e) { "Experienced a network issue when charging Invoice#${invoice.id}" }
                }
                else -> {
                    logger.error(e) { "Experienced an unknown issue when charging Invoice#${invoice.id}" }
                }
            }
        }
    }

    private fun determineSettlementRetries(invoice: Invoice, failureReason: String) {
        // Determine if this is the last invoice settlement_tries 
        // and then mark as Failed
        if(invoice.settlementTries == 3) {
            invoiceService.setInvoiceAsFailed(invoice.id, failureReason)
        } else {
            invoiceService.setInvoiceForPaymentRetry(invoice.id)
        } 
    }

}
