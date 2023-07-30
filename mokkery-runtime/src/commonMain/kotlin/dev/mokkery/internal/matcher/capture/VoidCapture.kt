package dev.mokkery.internal.matcher.capture

import dev.mokkery.matcher.capture.Capture

internal object VoidCapture : Capture<Any?> {
    override fun capture(value: Any?) = Unit

    override fun toString(): String = "void()"
}
