package dev.mokkery.test

actual typealias TestResult = Unit

actual fun runTest(block: suspend () -> Unit): TestResult {
    kotlinx.coroutines.test.runTest {
        block()
    }
}
