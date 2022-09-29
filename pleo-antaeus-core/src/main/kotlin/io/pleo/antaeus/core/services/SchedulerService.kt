package io.pleo.antaeus.core.services

import mu.KotlinLogging


private val logger = KotlinLogging.logger {}

class SchedulerService(
    private val billingService: BillingService
) {
    
    fun start() {
        billingService.start()
    }

}
