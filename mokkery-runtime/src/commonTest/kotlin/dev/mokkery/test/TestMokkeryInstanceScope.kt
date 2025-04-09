package dev.mokkery.test

import dev.mokkery.MockMode
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.context.MockContext
import dev.mokkery.internal.interceptor.MokkeryKind
import kotlin.reflect.KClass

internal class TestMokkeryInstanceScope(
    id: String = "mock@1",
    mode: MockMode = MockMode.strict,
    kind: MokkeryKind = MokkeryKind.Mock,
    interceptedTypes: List<KClass<*>> = listOf(Unit::class),
    typeArguments: List<KClass<*>> = emptyList(),
    spiedObject: Any? = null,
    interceptor: TestMockInterceptor = TestMockInterceptor(),
) : MokkeryInstanceScope {

    override val mokkeryContext: MokkeryContext = MockContext(
        id = id,
        mode = mode,
        kind = kind,
        interceptedTypes = interceptedTypes,
        typeArguments = typeArguments,
        thisInstanceScope = this,
        spiedObject = spiedObject,
    ) + interceptor
}
