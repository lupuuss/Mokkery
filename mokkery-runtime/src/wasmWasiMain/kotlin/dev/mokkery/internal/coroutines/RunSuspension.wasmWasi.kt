package dev.mokkery.internal.coroutines

internal actual fun runSuspension(block: suspend () -> Unit): Unit = error(
    "Coroutines are not supported for Wasm WASI in this Mokkery release!" +
            " Expect this support after kotlinx.coroutine release with Wasm WASI support!"
)
