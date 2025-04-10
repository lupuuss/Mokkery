package dev.mokkery.test

import dev.mokkery.MockMode
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MockId
import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.context.MockSpec
import dev.mokkery.internal.interceptor.MokkeryKind
import kotlin.reflect.KClass

internal class TestMokkeryInstanceScope(
    typeName: String = "mock",
    sequence: Long = 1,
    mode: MockMode = MockMode.strict,
    kind: MokkeryKind = MokkeryKind.Mock,
    interceptedTypes: List<KClass<*>> = listOf(Unit::class),
    typeArguments: List<KClass<*>> = emptyList(),
    spiedObject: Any? = null,
    interceptor: TestContextCallInterceptor = TestContextCallInterceptor(),
) : MokkeryInstanceScope {

    override val mokkeryContext: MokkeryContext = MockSpec(
        id = MockId(typeName, sequence),
        mode = mode,
        kind = kind,
        interceptedTypes = interceptedTypes,
        typeArguments = typeArguments,
        thisInstanceScope = this,
        spiedObject = spiedObject,
    ) + interceptor
}
