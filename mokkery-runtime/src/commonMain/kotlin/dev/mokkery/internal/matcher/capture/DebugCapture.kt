package dev.mokkery.internal.matcher.capture

import dev.mokkery.matcher.capture.Capture

internal data class DebugCapture<T>(
    val name: String?,
    val capture: Capture<T>,
): Capture<T> {
    private val tag = if (name != null) "Debug($name):" else "Debug(#${hashCode().toString(16)}):"
    override val values: List<T>
        get() = capture.values

    override fun capture(value: T) {
        println("$tag $value")
        capture.capture(value)
    }

    override fun toString(): String = "debug($capture)"
}
