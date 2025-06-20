package dev.mokkery.test

import dev.mokkery.MockMode
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.context.MokkeryInstanceSpec
import kotlin.reflect.KClass

internal class TestMokkeryInstanceScope(
    typeName: String = "mock",
    sequence: Long = 1,
    interceptedTypes: List<KClass<*>> = listOf(Unit::class),
    typeArguments: List<List<KClass<*>>> = List(interceptedTypes.size) { emptyList() },
    mode: MockMode? = MockMode.strict,
    spiedObject: Any? = null,
    interceptor: TestContextCallInterceptor = TestContextCallInterceptor(),
    context: MokkeryContext = MokkeryContext.Empty
) : MokkeryInstanceScope {

    override val mokkeryContext: MokkeryContext = MokkeryInstanceSpec.create(
        id = MokkeryInstanceId(typeName, sequence),
        thisRef = this,
        interceptedTypes = interceptedTypes,
        typeArguments = typeArguments,
        mode = mode,
        spiedObject = spiedObject,
    ) + interceptor + context
}
