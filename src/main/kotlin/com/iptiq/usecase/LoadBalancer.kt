package com.iptiq.usecase

import com.iptiq.domain.Provider
import com.iptiq.domain.exception.ProviderNotFoundException
import com.iptiq.domain.exception.ProviderUnavailableException
import java.lang.IllegalArgumentException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.scheduleAtFixedRate

abstract class LoadBalancer {
    var providers: List<Provider> = emptyList()
    //Number of request processed by each provider
    val requestPerProvider: Int = 2
    //parallel number of request count
    val requestCount = AtomicInteger()
    var healthCheckDelay: Long = 0
    var healthCheckPeriod: Long = 1000
    var isHealthCheckStarted: Boolean = false

    /*
        Registers a list of Providers
        @param providers - set of ids
        @throws IllegalArgumentException
     */
    fun register(providers: Set<Int>) = when(providers.size) {
        0 -> throw IllegalArgumentException("Providers cannot be empty.")
        else -> {
            if (providers.size > 10) {
                throw IllegalArgumentException("More than 10 providers cannot be registered.")
            } else {
                this.providers = providers.map { Provider(it) }
                //Start health check only after the providers are registered.
                if(!isHealthCheckStarted){
                    //Perform check every few seconds
                    Timer().scheduleAtFixedRate(healthCheckDelay, healthCheckPeriod) {
                        healthChecker()
                    }
                    isHealthCheckStarted = true
                } else {}
            }
        }
    }

    /**
     * Returns provider id
     * @return String - provider id
     * @throws ProviderNotFoundException
     *         ProviderUnavailableException
     */
    fun get() = when {
        providers.isEmpty() -> throw ProviderNotFoundException("No providers found.")
        else -> {
            // Fetch all alive and enabled providers
            val availableInstances = providers.filter { it.isAlive && it.isEnabled }.size
            // Checks request count against no. of parallel counts
            if ((requestPerProvider * availableInstances) <= requestCount.incrementAndGet()) {
                requestCount.decrementAndGet()
                throw ProviderUnavailableException("No provider available")
            }
            val provider = getProvider(providers).get()
            requestCount.decrementAndGet()
            provider
        }
    }

    /**
     * Returns provider
     * @param registerProviders
     * @return Provider
     */
    abstract fun getProvider(registerProviders: List<Provider>): Provider

    /**
     * Manually enables or disables the provider based on provider ID
     * @param providerID
     * @param isEnabled - true/false
     * @throws ProviderNotFoundException
     */
    fun setIsEnabled(providerID: Int, isEnabled: Boolean)  =
        providers.find { it.id == providerID }?.let {println("Found $it"); it.isEnabled = isEnabled }
            ?: throw ProviderNotFoundException("Provider with id $providerID not found.")

    /**
     * Checks the health of each provider.
     * Enables a provider when provider check is success for 2 consecutive times
     */
    fun healthChecker(){
        providers.filter { it.isEnabled }.forEach {
            when(it.check()){
                true -> {
                    it.heartBeatSuccessCount++
                    if(!it.isAlive && it.heartBeatSuccessCount == 2){
                        it.isAlive = true
                        it.heartBeatSuccessCount = 0
                    }
                }
                else -> {
                    it.heartBeatSuccessCount = 0
                    it.isAlive = false
                }
            }
        }
    }
}