package com.iptiq.usecase

import com.iptiq.domain.exception.ProviderNotFoundException
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.BeforeTest
import kotlin.test.assertFalse


class LoadBalancerTest {
    lateinit var lb: LoadBalancer

    @BeforeTest
    fun init(){
        lb = RandomLoadBalancer()
    }

    @Test
    fun testGetWithoutRegisteredList(){
        assertFailsWith<ProviderNotFoundException> { lb.get() }
    }

    @Test
    fun testRegisterForEmptySet(){
        assertFailsWith(
            exceptionClass = IllegalArgumentException::class,
            message = "Providers cannot be empty.",
            block = { lb.register(emptySet()) }
        )
    }

    @Test
    fun testRegisterForTenProviders(){
        lb.register(Array(10){ it }.toSet())
        assertEquals(10, lb.providers.size)
    }

    @Test
    fun testRegisterForLessThanTenProviders(){
        lb.register(Array(5){ it }.toSet())
        assertEquals(5, lb.providers.size)
        lb.register(Array(7){ it }.toSet())
        assertEquals(7, lb.providers.size)
    }

    @Test
    fun testSetIsEnabled(){
        val providers = setOf(1,2)
        lb.register(providers)
        lb.setIsEnabled(1, false)
        assertEquals(false, lb.providers.get(0).isEnabled)
        lb.setIsEnabled(1, true)
        assertTrue(lb.providers.get(0).isEnabled)
    }

    @Test
    fun testSetIsEnabledException(){
        val providers = setOf(1,2)
        lb.register(providers)
        assertFailsWith(
            exceptionClass = ProviderNotFoundException::class,
            message = "Provider with id 5 not found.",
            block = { lb.setIsEnabled(5, false) }
        )
    }

    @Test
    fun testHealthCheckerSuccess(){
        val providers = setOf(1,2)
        lb.register(providers)
        lb.healthChecker()
        assertTrue(lb.providers.get(0).isAlive)
        assertTrue(lb.providers.get(1).isAlive)
    }

    @Test
    fun testHealthCheckerForOneProviderInActive(){
        val providers = setOf(1,2)
        lb.register(providers)
        lb.providers.get(0).checkFlag = false
        lb.healthChecker()
        assertFalse(lb.providers.get(0).isAlive)
        assertTrue(lb.providers.get(1).isAlive)
    }

    @Test
    fun testHealthCheckerForReactivatingProvider(){
        val providers = setOf(1,2)
        lb.register(providers)
        lb.healthCheckDelay = 5000
        lb.providers.get(0).checkFlag = false
        lb.healthChecker()
        lb.providers.get(0).checkFlag = true
        lb.healthChecker()
        assertFalse(lb.providers.get(0).isAlive)
        assertEquals(1, lb.providers.get(0).heartBeatSuccessCount)
        lb.healthChecker()
        assertTrue(lb.providers.get(0).isAlive)
        assertEquals(0, lb.providers.get(0).heartBeatSuccessCount)
    }

    @Test
    fun testHealthCheckerEveryOneSec(){
        val providers = setOf(1,2)
        lb.healthCheckDelay = 5000
        lb.register(providers)
        lb.providers.get(0).checkFlag = false
        lb.healthChecker()
        lb.providers.get(0).checkFlag = true
        lb.healthChecker()
        assertFalse(lb.providers.get(0).isAlive)
        assertEquals(1, lb.providers.get(0).heartBeatSuccessCount)
        lb.healthChecker()
        assertTrue(lb.providers.get(0).isAlive)
        assertEquals(0, lb.providers.get(0).heartBeatSuccessCount)
    }
}