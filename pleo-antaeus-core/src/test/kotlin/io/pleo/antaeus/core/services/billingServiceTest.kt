package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class BillingServiceTest {
    private val paymentProvider = mockk<PaymentProvider>()
    private val invoiceService = mockk<InvoiceService>()

    private val billingService = BillingService(
        invoiceService = invoiceService,
        paymentProvider = paymentProvider
    )

    fun createMockInvoices(): list<Invoice> {
        return listOf(
                Invoice(
                    id = 123,
                    customerId = 456,
                    amount = Money(
                        value = BigDecimal.valueOf(20),
                        currency = Currency.USD
                    ),
                    status = InvoiceStatus.PENDING,
                    settlementTries = 0,
                    failureReason = null
                )
            )
    }

    @Test
    fun `set a notice status to paid`() {
        every { paymentProvider.charge(any()) } returns true
        every { invoiceService.fetchPendingInvoices() } returns createMockInvoices()
            

        billingService.run()

        verify(exactly = 1) { invoiceService.setInvoiceAsPaid(eq(1)) }
    }

    @Test
    fun `set a notice status to failed`() {
        every { paymentProvider.charge(any()) } returns false
        every { invoiceService.fetchPendingInvoices() } returns createMockInvoices()

        billingService.run()

        verify(exactly = 1) { invoiceService.setInvoiceAsFailed(eq(1)) }
    }

    @Test
    fun `set invoice for retry on network error`() {
        every { paymentProvider.charge(any()) } throws NetworkException()
        every { invoiceService.fetchPendingInvoices() } returns createMockInvoices()

        billingService.run()

        verify(exactly = 1) { invoiceService.setInvoiceForPaymentRetry(eq(1)) }
    }

    
}
