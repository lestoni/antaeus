/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus

class InvoiceService(private val dal: AntaeusDal) {
    fun fetchAll(): List<Invoice> {
        return dal.fetchInvoices()
    }

    fun fetch(id: Int): Invoice {
        return dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }

    fun setInvoiceAsPaid(id: Int): Invoice {
        return dal.updateInvoiceStatus(id, InvoiceStatus.PAID, null) ?: throw InvoiceNotFoundException(id)
    }

    fun setInvoiceAsFailed(id: Int, failureReason: String): Invoice {
        return dal.updateInvoiceStatus(id, InvoiceStatus.FAILED, failureReason) ?: throw InvoiceNotFoundException(id)
    } 

    fun setInvoiceForPaymentRetry(id: Int): Invoice {
        return dal.updateInvoiceSettlementTries(id) ?: throw InvoiceNotFoundException(id)
    }

    fun fetchPendingInvoices(): List<Invoice> {
        return dal.fetchInvoicesByStatus(InvoiceStatus.PENDING)
    }
}
