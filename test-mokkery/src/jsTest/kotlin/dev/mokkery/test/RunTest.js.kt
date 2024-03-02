package dev.mokkery.test

import kotlin.js.Promise

@Suppress("ACTUAL_WITHOUT_EXPECT", "ACTUAL_TYPE_ALIAS_TO_CLASS_WITH_DECLARATION_SITE_VARIANCE")
actual typealias TestResult = Promise<Unit>

actual fun runTest(block: suspend () -> Unit): TestResult = kotlinx.coroutines.test.runTest {
    block()
}
