package dev.mokkery.test

actual val Platform.Companion.current: Platform get() = AndroidTarget

data object AndroidTarget : Platform.Jvm

