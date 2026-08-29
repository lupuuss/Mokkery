package dev.mokkery.test

import dev.mokkery.MockMode
import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.context.Function
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.context.MokkeryInstanceSpec
import kotlin.reflect.KClass

internal class TestMokkeryInstanceScope(
    instanceId: Long = 1,
    typeName: String = "mock",
    interceptedTypes: List<KClass<*>> = listOf(Unit::class),
    typeArguments: List<List<KClass<*>>> = List(interceptedTypes.size) { emptyList() },
    mode: MockMode? = MockMode.strict,
    spiedObject: Any? = null,
    functions: List<Function> = emptyList(),
    interceptor: TestContextCallInterceptor = TestContextCallInterceptor(),
    context: MokkeryContext = MokkeryContext.Empty
) : MokkeryInstanceScope {

    override val mokkeryContext: MokkeryContext = MokkeryInstanceSpec.create(
        id = MokkeryInstanceId(typeName, instanceId),
        thisRef = this,
        interceptedTypes = interceptedTypes,
        typeArguments = typeArguments,
        mode = mode,
        spiedObject = spiedObject,
    ) + interceptor + TestInstanceContracts() + TestMemberFunctions(functions) + context
}
