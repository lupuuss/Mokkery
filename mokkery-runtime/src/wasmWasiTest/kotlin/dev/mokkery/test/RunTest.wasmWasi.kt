package dev.mokkery.test

actual typealias TestResult = Unit

actual fun runTest(block: suspend () -> Unit) {
    println("Suspendable test are skipped for Wasm WASI!")
}
