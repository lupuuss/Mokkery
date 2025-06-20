package dev.mokkery.test

import dev.mokkery.MockMode
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MockId
import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.context.MockSpec
import dev.mokkery.internal.MokkeryKind
import dev.mokkery.internal.context.TypeSpec
import kotlin.reflect.KClass

internal class TestMokkeryInstanceScope(
    typeName: String = "mock",
    sequence: Long = 1,
    mode: MockMode = MockMode.strict,
    kind: MokkeryKind = MokkeryKind.Mock,
    interceptedTypes: List<KClass<*>> = listOf(Unit::class),
    typeArguments: List<List<KClass<*>>> = List(interceptedTypes.size) { emptyList() },
    spiedObject: Any? = null,
    interceptor: TestContextCallInterceptor = TestContextCallInterceptor(),
    context: MokkeryContext = MokkeryContext.Empty
) : MokkeryInstanceScope {

    override val mokkeryContext: MokkeryContext = MockSpec(
        id = MockId(typeName, sequence),
        mode = mode,
        kind = kind,
        interceptedTypes = interceptedTypes.mapIndexed { index, it -> TypeSpec(it, typeArguments[index]) },
        thisRef = this,
        spiedObject = spiedObject,
    ) + interceptor + context
}
