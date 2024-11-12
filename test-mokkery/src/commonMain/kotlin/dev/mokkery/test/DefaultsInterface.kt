package dev.mokkery.test

interface DefaultsInterfaceLevel1<T> : DefaultsInterfaceLevel2<T> {

    fun callNoDefault(): Unit

    override var Int.extProperty: Double
        get() = this.toDouble()
        set(value) { }

    override fun call(a: Int, b: ComplexType): ComplexType = ComplexType(a.toString())

    override suspend fun callSuspend(a: Int, b: ComplexType): ComplexType = ComplexType(a.toString())

    override fun Int.callExtension(input: ComplexType): ComplexType = ComplexType(this.toString())
}

interface DefaultsInterfaceLevel2<T> {

    var property: T?
        get() = null
        set(value) { }

    var Int.extProperty: Double
        get() = this.toDouble() + 1.0
        set(value) { }

    var <R : T> R.extPropertyGeneric: R
        get() = this
        set(value) { }

    fun call(a: Int, b: ComplexType = ComplexType): ComplexType = ComplexType((a + 1).toString())

    suspend fun callSuspend(a: Int, b: ComplexType = ComplexType): ComplexType =  ComplexType((a + 1).toString())

    fun Int.callExtension(input: ComplexType = ComplexType): ComplexType =  ComplexType((this + 1).toString())

    fun callIndirectDefault(a: Int, b: ComplexType = ComplexType) = ComplexType((a + 1).toString())

    suspend fun callSuspendIndirectDefault(a: Int, b: ComplexType = ComplexType) = ComplexType((a + 1).toString())
}
