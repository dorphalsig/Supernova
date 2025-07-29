package com.supernova.util

/** Simple abstraction over time retrieval for testability. */
fun interface Clock {
    fun currentTimeMillis(): Long
}

object SystemClock : Clock {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
