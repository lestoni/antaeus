package io.pleo.antaeus.core.services

import mu.KotlinLogging
import io.pleo.antaeus.core.services.BillingService
import java.time.Clock
import java.time.LocalDate
import kotlin.concurrent.timer
import kotlin.random.Random


private val logger = KotlinLogging.logger {}

/**
 * As a contract any service that needs to schedule needs to adhere to a contract
 * of having Class::start() method
 * */

class SchedulerService(
    private val billingService: BillingService
) {

    private val clock: Clock = Clock.systemDefaultZone()
    
    fun run() {
        // FIXME: This can be abstracted to only handle billing service only
        // aka billingSchedulerService and any other scheduler
        // Incase there's more service jobs to schedule in here then it will
        // beat single-responsibility principle. For now this will focus on billing only
        logger.info { "Begin Running Scheduled " }
        scheduleJob()
    }

    private fun scheduleJob() {
        timer(name = billingService::class.simpleName, daemon = true, period = 5000, initialDelay= Random.nextLong(5000)) {
            startJob()
        }
    }

    private fun startJob() {
        // We determine day of the Month which should the first day
        val dayOfMonth = LocalDate.now(clock).dayOfMonth
        if(dayOfMonth != 1) {
            logger.info { "Scheduled Job Skipped" }
            return
        }

        try {
            billingService.run()
        } catch(ex: Exception) {
            logger.error(ex) { "Experienced an issue for billingService" }
        }
    }

}
