package dev.mokkery.internal

internal actual val Any.mokkeryScope: MokkeryInstanceScope?
    get() = this as? MokkeryInstanceScope ?: jsFunctionMokkeryScope

@Suppress("unused")
internal fun MokkeryInstanceScope.initializeInJsFunctionMock(function: Any) {
    function.jsFunctionMokkeryScope = this
}

internal inline var Any.jsFunctionMokkeryScope: MokkeryInstanceScope?
    get() = this.asDynamic()._mokkeryScope as? MokkeryInstanceScope
    set(value) {
        this.asDynamic()._mokkeryScope = value
    }
