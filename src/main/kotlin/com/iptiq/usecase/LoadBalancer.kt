package com.iptiq.usecase

import com.iptiq.domain.Provider
import com.iptiq.domain.exception.ProviderNotFoundException
import com.iptiq.domain.exception.ProviderUnavailableException
import java.lang.IllegalArgumentException
import java.lang.Thread.sleep
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.coroutines.coroutineContext

abstract class LoadBalancer {
    var providers: List<Provider> = emptyList()
    val requestPerProvider: Int = 2
    var requestCount: Int = 0
    var healthCheckDelay: Long = 0
    var healthCheckPeriod: Long = 1000
    var isHealthCheckStarted: Boolean = false


    fun register(providers: Set<Int>) = when(providers.size) {
        0 -> throw IllegalArgumentException("Providers cannot be empty.")
        else -> {
            if (providers.size > 10) {
                throw IllegalArgumentException("More than 10 providers cannot be registered.")
            } else {
                this.providers = providers.map { Provider(it) }
                if(!isHealthCheckStarted){
                    Timer().scheduleAtFixedRate(healthCheckDelay, healthCheckPeriod) {
                        healthChecker()
                    }
                    isHealthCheckStarted = true
                } else {}
            }
        }
    }


    fun get() = when {
        providers.isEmpty() -> throw ProviderNotFoundException("No providers found.")
        else -> {
            requestCount++
            val availableInstances = providers.filter { it.isAlive && it.isEnabled }.size
            println("requestCount is $requestCount *************")
            if ((requestPerProvider * availableInstances) <= requestCount) {
                requestCount--
                throw ProviderUnavailableException("No provider available")
            }
            val provider = getProvider(providers).get()
            requestCount--
            provider
        }
    }

    abstract fun getProvider(registerProviders: List<Provider>): Provider

    fun setIsEnabled(providerID: Int, isEnabled: Boolean)  =
        providers.find { it.id == providerID }?.let {println("Found $it"); it.isEnabled = isEnabled }
            ?: throw ProviderNotFoundException("Provider with id $providerID not found.")


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