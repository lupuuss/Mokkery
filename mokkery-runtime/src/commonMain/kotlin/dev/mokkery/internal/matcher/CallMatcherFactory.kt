package dev.mokkery.internal.matcher

import dev.mokkery.internal.defaults.DefaultsMaterializer
import dev.mokkery.internal.names.SignatureGenerator
import dev.mokkery.internal.MokkeryCollection

internal fun interface CallMatcherFactory {

    fun create(collection: MokkeryCollection): CallMatcher
}

internal fun CallMatcherFactory(
    signatureGenerator: SignatureGenerator
): CallMatcherFactory = CallMatcherFactory { mocks ->
    CallMatcher(signatureGenerator, DefaultsMaterializer(mocks))
}
