package com.iptiq.usecase

import com.iptiq.domain.Provider
import com.iptiq.domain.exception.ProviderUnavailableException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.BeforeTest

class RoundRobinLoadBalancerTest {
    lateinit var roundRobinObj: LoadBalancer
    lateinit var registeredList: List<Provider>

    @BeforeTest
    fun init(){
        roundRobinObj = RoundRobinLoadBalancer()
    }

    @Test
    fun testGetProvider(){
        registeredList = listOf(Provider(1), Provider(2), Provider(3))
        assertEquals("1",roundRobinObj.getProvider(registeredList).get())
        assertEquals("2",roundRobinObj.getProvider(registeredList).get())
        assertEquals("3",roundRobinObj.getProvider(registeredList).get())
    }

    @Test
    fun testGetProviderRepeat(){
        registeredList = listOf(Provider(1),Provider(2))
        assertEquals("1",roundRobinObj.getProvider(registeredList).get())
        assertEquals("2",roundRobinObj.getProvider(registeredList).get())
        assertEquals("1",roundRobinObj.getProvider(registeredList).get())
    }

    @Test
    fun testGetProviderExceptionForIsEnabledFalse() {
        registeredList = listOf(
            Provider(1,isAlive = false),
            Provider(2,isAlive = false)
        )
        assertFailsWith<ProviderUnavailableException> { roundRobinObj.getProvider(registeredList) }
    }

    @Test
    fun testGetProviderForChangingFlag() {
        registeredList = listOf(Provider(1, isAlive = false), Provider(2), Provider(3, isEnabled = false))
        assertEquals("2",roundRobinObj.getProvider(registeredList).get())
        registeredList.get(0).isAlive = true //  set provider 1 to active
        assertEquals("1",roundRobinObj.getProvider(registeredList).get())
        registeredList.get(1).isEnabled = false // disable provider 2
        registeredList.get(2).isEnabled = true // enable provider 3
        assertEquals("3",roundRobinObj.getProvider(registeredList).get())
    }
}