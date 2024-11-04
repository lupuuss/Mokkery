package dev.mokkery.test

actual val Platform.Companion.current: Platform get() = JsTarget

data object JsTarget : Platform.Js
