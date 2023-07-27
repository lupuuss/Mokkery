package dev.mokkery.internal.matcher.capture

import dev.mokkery.matcher.capture.Capture

internal class VoidCapture<T> : Capture<T> {
    override val values = emptyList<T>()

    override fun capture(value: T) = Unit

    override fun toString(): String = "void()"
}
