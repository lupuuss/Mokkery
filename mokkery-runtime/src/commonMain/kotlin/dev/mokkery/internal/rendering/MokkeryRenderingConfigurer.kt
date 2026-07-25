package dev.mokkery.internal.rendering

import dev.mokkery.context.MokkeryContext

internal interface MokkeryRenderingConfigurer {

    var mokkeryContext: MokkeryContext
}

internal fun MokkeryRenderingConfigurer(
    context: MokkeryContext
): MokkeryRenderingConfigurer = object : MokkeryRenderingConfigurer {
    override var mokkeryContext: MokkeryContext = context
}

context(builder: MokkeryRenderingConfigurer)
internal operator fun MokkeryContext.unaryPlus() {
    builder.mokkeryContext += this
}

context(builder: MokkeryRenderingConfigurer)
internal operator fun MokkeryContext.Element.unaryMinus() {
    builder.mokkeryContext -= this.key
}
