package dev.mokkery.test

interface SpyTestInterface<Type> {

    var property: Type?

    fun call(input: ComplexType): ComplexType

    var Type.extProperty: String

    fun Type.callExtension(flag: Boolean = true): Int

    suspend fun <T : Type> callSuspend(value: T, flag: Boolean = true): T

    suspend fun callResult(result: Result<Type>): Result<Type>

    fun callNothing(a: Int = 10, type: ComplexType = ComplexType): Nothing

    companion object {

        operator fun <Type> invoke(): SpyTestInterface<Type> = object : SpyTestInterface<Type> {

            override var property: Type? = null

            override fun call(input: ComplexType): ComplexType = ComplexType(input.id.toInt().plus(1).toString())

            override var Type.extProperty: String
                get() = this.toString()
                set(value) {
                    throw IllegalArgumentException("$this - $value")
                }

            override fun Type.callExtension(flag: Boolean): Int = this.hashCode()

            override suspend fun <T : Type> callSuspend(value: T, flag: Boolean): T = value

            override suspend fun callResult(result: Result<Type>): Result<Type & Any> = result.mapCatching { it!! }

            override fun callNothing(a: Int, type: ComplexType): Nothing = throw IllegalArgumentException("Failed!")

        }

    }
}
