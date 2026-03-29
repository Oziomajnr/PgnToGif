package com.chunkymonkey.pgntogifconverter.util

/**
 * True when this process runs under Android instrumented tests.
 * Uses reflection so the main APK does not depend on androidx.test at compile time.
 */
object TestProcess {

    fun isInstrumentedTest(): Boolean = try {
        val clazz = Class.forName("androidx.test.platform.app.InstrumentationRegistry")
        val m = clazz.getMethod("getInstrumentation")
        m.invoke(null) != null
    } catch (_: Throwable) {
        false
    }
}
