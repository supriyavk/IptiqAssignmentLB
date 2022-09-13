package com.iptiq.usecase

import com.iptiq.domain.Provider
import com.iptiq.domain.exception.ProviderNotFoundException
import com.iptiq.domain.exception.ProviderUnavailableException

/**
 * Random load balancer
 *
 * @constructor Create empty Random load balancer
 */
class RandomLoadBalancer: LoadBalancer() {
    /**
     * Get provider based on random algorithm
     * @param registerProviders - List of providers
     * @return Provider
     * @throws ProviderUnavailableException
     */
    override fun getProvider(registerProviders: List<Provider>) =
        registerProviders.filter { it.isEnabled && it.isAlive }.
        let {
            if(it.isEmpty())
                throw ProviderUnavailableException("No active provider found.")
            else
                it.random()
        }
}