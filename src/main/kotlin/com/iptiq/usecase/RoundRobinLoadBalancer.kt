package com.iptiq.usecase

import com.iptiq.domain.Provider
import com.iptiq.domain.exception.ProviderNotFoundException
import com.iptiq.domain.exception.ProviderUnavailableException

class RoundRobinLoadBalancer: LoadBalancer() {
    private var nextIndex = 0;



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