package dev.mokkery.internal.defaults

// mock/spy instance implements it
@PublishedApi
internal interface DefaultsExtractorFactory {

    fun mokkeryCreateExtractor(): Any
}
