package dev.mokkery.internal.context

import dev.mokkery.context.MokkeryContext

internal val GlobalMokkeryContext: MokkeryContext = createGlobalContext()

private fun createGlobalContext(): MokkeryContext = MokkeryTools.default()
