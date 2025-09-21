package dev.mokkery.test

interface   ContextParametersInterface {

    context(type: ComplexType, i: Int)
    fun String.callCtxAndExt(): String

    fun ComplexType.callExt(): String

    context(type: ComplexType, i: Int)
    fun callCtx(): String
}
