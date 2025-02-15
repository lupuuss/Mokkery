package dev.mokkery.internal

import dev.mokkery.MokkerySuiteScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.context.MokkeryTools

internal object GlobalMokkeryScope : MokkerySuiteScope {

    override val mokkeryContext: MokkeryContext = MokkeryTools.default()
}
