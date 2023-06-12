@file:Suppress("UNUSED_PARAMETER")

package dev.mokkery

public enum class MockMode {
    Autofill, Strict, AutoUnit;

    public companion object {
        public val Default: MockMode = Strict
    }
}

public inline fun <reified T> mock(mode: MockMode = MockMode.Default): T = throw MokkeryPluginNotAppliedException()

