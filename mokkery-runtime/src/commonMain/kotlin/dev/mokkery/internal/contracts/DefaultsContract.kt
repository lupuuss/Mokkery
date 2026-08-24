package dev.mokkery.internal.contracts

// mock/spy instance implements it
@PublishedApi
internal interface DefaultsContract : InstanceContract {

    fun mokkeryCreateExtractor(functionId: Long): Any
}
