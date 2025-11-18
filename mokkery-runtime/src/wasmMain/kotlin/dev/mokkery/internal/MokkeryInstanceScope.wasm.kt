package dev.mokkery.internal

import dev.mokkery.MokkeryInstanceScope

internal actual val Any.mokkeryScope: MokkeryInstanceScope?
    get() = this as? MokkeryInstanceScope
