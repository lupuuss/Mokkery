package dev.mokkery.test

actual val Platform.Companion.current: Platform get() = SomeNativeTarget

data object SomeNativeTarget : Platform.Native
