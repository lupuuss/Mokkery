package dev.mokkery.internal.contracts

import dev.mokkery.context.Function

// mock/spy instance implements it
@PublishedApi
internal interface DefaultsContract : InstanceContract {

    fun mokkeryCreateExtractor(functionName: String, parameters: List<Function.Parameter>): Any
}
