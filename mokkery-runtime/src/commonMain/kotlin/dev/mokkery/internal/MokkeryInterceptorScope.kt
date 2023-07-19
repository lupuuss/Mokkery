package dev.mokkery.internal

import dev.mokkery.MockMode

internal interface MokkeryInterceptorScope {

    val id: String

    val interceptor: MokkeryInterceptor
}

internal interface MokkerySpyScope : MokkeryInterceptorScope {

    override val interceptor: MokkerySpy
}


internal interface MokkeryMockScope : MokkerySpyScope {

    override val interceptor: MokkeryMock
}


internal fun MokkerySpyScope(typeName: String): MokkerySpyScope {
    return DynamicMokkerySpyScope(typeName)
}

internal fun MokkeryMockScope(mode: MockMode, typeName: String): MokkeryMockScope {
    return DynamicMokkeryMockScope(mode, typeName)
}

private class DynamicMokkeryMockScope(
    mode: MockMode,
    typeName: String,
) : MokkeryMockScope {
    override val interceptor = MokkeryMock(mode)

    override val id = "$typeName@${hashCode().toString(33)}"
}

private class DynamicMokkerySpyScope(typeName: String) : MokkerySpyScope {

    override val interceptor = MokkerySpy()

    override val id = "$typeName@${hashCode().toString(33)}"
}
