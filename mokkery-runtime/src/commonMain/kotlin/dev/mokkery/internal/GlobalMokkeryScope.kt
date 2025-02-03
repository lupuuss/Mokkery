package dev.mokkery.internal

import dev.mokkery.MokkeryTestsScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.context.IgnoringMocksRegistry
import dev.mokkery.internal.context.MokkeryTools

internal object GlobalMokkeryScope : MokkeryTestsScope {

    override val mokkeryContext: MokkeryContext = MokkeryTools.default() + IgnoringMocksRegistry
}
