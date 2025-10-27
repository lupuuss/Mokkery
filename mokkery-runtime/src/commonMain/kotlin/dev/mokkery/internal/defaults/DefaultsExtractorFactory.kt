package dev.mokkery.internal.defaults

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.internal.MokkeryInstanceScope

internal interface DefaultsExtractorFactory : MokkeryContext.Element {

    override val key get() = Key

    fun createDefaultsExtractor(): Any

    companion object Key : MokkeryContext.Key<DefaultsExtractorFactory>
}

internal val MokkeryInstanceScope.defaultsExtractorFactory: DefaultsExtractorFactory
    get() = mokkeryContext.require(DefaultsExtractorFactory)

internal class ArgumentsExtractedException(
    val values: List<Any?>
) : MokkeryRuntimeException("This exception should be caught by the internal machinery!")
