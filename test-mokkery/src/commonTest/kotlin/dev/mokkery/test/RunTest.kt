package dev.mokkery.test

expect class TestResult

expect annotation class IgnoreOnWasmWasi()

expect suspend fun delay(duration: Long)

expect fun runTest(block: suspend () -> Unit): TestResult
