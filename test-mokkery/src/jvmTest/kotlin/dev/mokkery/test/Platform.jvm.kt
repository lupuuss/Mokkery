package dev.mokkery.test

actual val Platform.Companion.current: Platform get() = JvmTarget

data object JvmTarget : Platform.Jvm
