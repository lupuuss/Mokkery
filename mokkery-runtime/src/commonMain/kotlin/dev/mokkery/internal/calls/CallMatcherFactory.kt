package dev.mokkery.internal.calls

import dev.mokkery.internal.defaults.DefaultsMaterializer
import dev.mokkery.internal.names.SignatureGenerator
import dev.mokkery.internal.MocksCollection

internal fun interface CallMatcherFactory {

    fun create(mocks: MocksCollection): CallMatcher
}

internal fun CallMatcherFactory(
    signatureGenerator: SignatureGenerator
): CallMatcherFactory = CallMatcherFactory { mocks ->
    CallMatcher(signatureGenerator, DefaultsMaterializer(mocks))
}
