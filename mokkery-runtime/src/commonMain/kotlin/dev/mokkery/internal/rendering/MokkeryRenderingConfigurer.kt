package dev.mokkery.internal.rendering

import dev.mokkery.configurer.MokkeryConfigurer
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.configurer.BaseMokkeryConfigurer

internal interface MokkeryRenderingConfigurer : MokkeryConfigurer

internal fun MokkeryRenderingConfigurer(
    context: MokkeryContext
): MokkeryRenderingConfigurer = object : BaseMokkeryConfigurer(context), MokkeryRenderingConfigurer {}
