package com.iptiq.usecase

import com.iptiq.domain.Provider
import com.iptiq.domain.exception.ProviderNotFoundException
import com.iptiq.domain.exception.ProviderUnavailableException

/**
 * Round-robin load balancer
 *
 * @constructor Create empty Round robin load balancer
 */
class RoundRobinLoadBalancer: LoadBalancer() {
    private var nextIndex = 0;
    /**
     * Get provider based on round-robin algorithm
     * @param registerProviders - List of providers
     * @return Provider
     * @throws ProviderUnavailableException
     */
    override fun getProvider(registerProviders: List<Provider>) :Provider {
        var obj: Provider?
        for(i in registerProviders.indices){
            if (registerProviders.size <= nextIndex) {
                nextIndex = 0
            }
            obj = registerProviders.get(nextIndex++)
            if(obj.isAlive && obj.isEnabled){
                return obj;
            }
        }
        throw ProviderUnavailableException("No provider available.")
    }
}