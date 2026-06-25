package dev.mokkery.internal.rendering

import dev.mokkery.context.MokkeryContext

internal interface RenderingConfigurer {

    var context: MokkeryContext
}

internal fun RenderingConfigurer(context: MokkeryContext): RenderingConfigurer {
    return object : RenderingConfigurer {
        override var context: MokkeryContext = context
    }
}

context(builder: RenderingConfigurer)
internal operator fun MokkeryContext.unaryPlus() {
    builder.context += this
}

context(builder: RenderingConfigurer)
internal operator fun MokkeryContext.Element.unaryMinus() {
    builder.context -= this.key
}
