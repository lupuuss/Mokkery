package dev.mokkery.plugin

import dev.mokkery.context.MokkeryContext

interface MokkeryPluginScope {

    val mokkeryContext: MokkeryContext
}

fun MokkeryPluginScope(context: MokkeryContext) : MokkeryPluginScope {
    return object : MokkeryPluginScope {
        override val mokkeryContext: MokkeryContext = context
    }
}
