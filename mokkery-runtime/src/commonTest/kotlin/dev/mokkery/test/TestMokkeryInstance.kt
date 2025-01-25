package dev.mokkery.test

import dev.mokkery.MockMode
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MokkeryInstance
import dev.mokkery.internal.context.CurrentMockContext
import dev.mokkery.internal.interceptor.MokkeryKind
import kotlin.reflect.KClass

internal class TestMokkeryInstance(
    id: String = "mock@1",
    mode: MockMode = MockMode.strict,
    kind: MokkeryKind = MokkeryKind.Mock,
    interceptedTypes: List<KClass<*>> = listOf(Unit::class),
    override val mokkeryInterceptor: TestMokkeryCallInterceptor = TestMokkeryCallInterceptor(),
) : MokkeryInstance {

    override val mokkeryContext: MokkeryContext = CurrentMockContext(id, mode, kind, interceptedTypes, this)
}
