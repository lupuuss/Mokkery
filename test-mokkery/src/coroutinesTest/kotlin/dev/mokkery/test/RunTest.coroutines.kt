package dev.mokkery.test

actual typealias IgnoreOnWasmWasi = NoIgnore

annotation class NoIgnore

actual suspend fun delay(duration: Long) {
    kotlinx.coroutines.delay(duration)
}
