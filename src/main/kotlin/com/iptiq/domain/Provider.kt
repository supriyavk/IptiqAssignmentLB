package com.iptiq.domain

import com.sun.org.slf4j.internal.LoggerFactory

data class Provider(
    val id: Int,
    var heartBeatSuccessCount: Int = 0,
    var isAlive: Boolean = true,
    var isEnabled: Boolean = true
) {
    private val logger = LoggerFactory.getLogger(Provider::class.java)
    var checkFlag: Boolean = true
    fun get() = this.id.toString()
    fun check(): Boolean {
        logger.debug("Executing provider check")
        return checkFlag
    }
}