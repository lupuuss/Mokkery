package dev.mokkery.internal

import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.context.MokkeryTools

internal object GlobalMokkeryScope : MokkeryScope {

    override val mokkeryContext: MokkeryContext = MokkeryTools.default()
}
