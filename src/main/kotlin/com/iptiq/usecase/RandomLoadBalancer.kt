package com.iptiq.usecase

import com.iptiq.domain.Provider
import com.iptiq.domain.exception.ProviderNotFoundException
import com.iptiq.domain.exception.ProviderUnavailableException

class RandomLoadBalancer: LoadBalancer() {
//    override fun get() = when {
//        providers.isEmpty() -> throw ProviderNotFoundException("No providers found.")
//        else -> {
//            requestCount++
//            val availableInstances = providers.filter { it.isAlive && it.isEnabled }.size
//            if ((requestPerProvider * availableInstances) <= requestCount) {
//                requestCount--
//                throw ProviderUnavailableException("No provider available")
//            }
//            val provider = getProvider(providers).get()
//            requestCount--
//            provider
//        }
//    }

    override fun getProvider(registerProviders: List<Provider>) =
        registerProviders.filter { it.isEnabled && it.isAlive }.
        let {
            if(it.isEmpty())
                throw ProviderUnavailableException("No active provider found.")
            else
                it.random()
        }
}