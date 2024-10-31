package dev.mokkery.test

actual val Platform.Companion.current: Platform get() = SomeWasmTarget

data object SomeWasmTarget : Platform.Wasm
