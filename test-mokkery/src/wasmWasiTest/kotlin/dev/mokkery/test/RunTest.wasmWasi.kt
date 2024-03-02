package dev.mokkery.test


actual typealias TestResult = Unit

actual typealias IgnoreOnWasmWasi = kotlin.test.Ignore

actual fun runTest(block: suspend () -> Unit) {
    println("Suspendable test are skipped for Wasm WASI!")
}

actual suspend fun delay(duration: Long) {
    error("Delay should not be called on Wasm WASI")
}
