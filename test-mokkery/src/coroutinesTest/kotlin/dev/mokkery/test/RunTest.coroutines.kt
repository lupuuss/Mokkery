package dev.mokkery.test

actual typealias IgnoreOnWasmWasi = NoIgnore

annotation class NoIgnore

actual suspend fun delay(duration: Long) {
    kotlinx.coroutines.delay(duration)
}

@Suppress("ACTUAL_TYPE_ALIAS_NOT_TO_CLASS", "ACTUAL_WITHOUT_EXPECT")
actual typealias TestResult = kotlinx.coroutines.test.TestResult

actual fun runTest(block: suspend () -> Unit): TestResult = kotlinx.coroutines.test.runTest {
    block()
}
