package dev.mokkery.internal.defaults

import dev.mokkery.context.Function

// mock/spy instance implements it
@PublishedApi
internal interface DefaultsExtractorFactory {

    fun mokkeryCreateExtractor(functionName: String, parameters: List<Function.Parameter>): Any
}
