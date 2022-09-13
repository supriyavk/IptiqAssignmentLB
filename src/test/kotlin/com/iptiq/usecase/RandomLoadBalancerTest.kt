package com.iptiq.usecase

import com.iptiq.domain.exception.ProviderUnavailableException
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class RandomLoadBalancerTest {
    lateinit var randomLB: LoadBalancer

    @BeforeTest
    fun init(){
        randomLB = RandomLoadBalancer()
    }

    @Test
    fun testGetProvider(){
        randomLB.register(setOf(1,2,3))
        assertNotNull(randomLB.get())
        assertNotNull(randomLB.get())
        assertNotNull(randomLB.get())
    }

    @Test
    fun testGetProviderExceptionForIsAliveFalse() {
        randomLB.register(setOf(1,2))
        randomLB.providers.forEach { it.isAlive = false }
        assertFailsWith<ProviderUnavailableException> { randomLB.get() }
    }

    @Test
    fun testGetProviderExceptionForIsEnabledFalse() {
        randomLB.register(setOf(1,2))
        randomLB.setIsEnabled(1, false)
        randomLB.setIsEnabled(2, false)
        assertFailsWith<ProviderUnavailableException> { randomLB.get() }
    }
    @Test
    fun testGetProviderCheckOnlyActiveInstance() {
        randomLB.register(setOf(1,2,3))
        randomLB.setIsEnabled(3, false)
        randomLB.providers.get(1).isAlive = false // inAactivating provider 2
        assertEquals("1",randomLB.get())
        assertEquals("1",randomLB.get())
    }
}