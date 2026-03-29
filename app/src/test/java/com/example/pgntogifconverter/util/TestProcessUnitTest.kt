package com.example.pgntogifconverter.util

import com.chunkymonkey.pgntogifconverter.util.TestProcess
import org.junit.Assert.assertFalse
import org.junit.Test

class TestProcessUnitTest {

    @Test
    fun isInstrumentedTest_isFalseOnJvmUnitTests() {
        assertFalse(TestProcess.isInstrumentedTest())
    }
}
