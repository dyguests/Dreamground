package com.fanhl.dreamground

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class SkyClockViewTest {
    @Test
    fun calendar_test() {
        val instance = Calendar.getInstance()

        println(instance.time.time)
        Thread.sleep(100)
        println(instance.time.time)
        Thread.sleep(100)
        println(instance.time.time)

        assertEquals(4, (2 + 2).toLong())
    }
}