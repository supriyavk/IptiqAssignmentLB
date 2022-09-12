package com.iptiq.domain

data class Provider(
    val id: Int,
    var heartBeatSuccessCount: Int = 0,
    var isAlive: Boolean = true,
    var isEnabled: Boolean = true
) {
    var checkFlag: Boolean = true
    fun get() = this.id.toString()
    fun check(): Boolean {
        println("Executing provider check ")
        return checkFlag
    }
}