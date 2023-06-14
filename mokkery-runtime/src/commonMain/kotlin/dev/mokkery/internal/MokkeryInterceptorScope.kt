package dev.mokkery.internal

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
