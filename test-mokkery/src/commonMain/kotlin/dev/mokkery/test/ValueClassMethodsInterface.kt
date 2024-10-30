package dev.mokkery.test

interface ValueClassMethodsInterface<T> {

    fun callPrimitiveResult(result: Result<Int>): Result<Int>

    fun callComplexResult(result: Result<ComplexType>): Result<ComplexType>

    fun callParentGenericResult(result: Result<T>): Result<T>

    fun <R : T> callGenericResult(result: Result<R>): Result<R>

    fun callPrimitiveValueClass(result: PrimitiveValueClass): PrimitiveValueClass

    fun callValueClass(result: ValueClass<ComplexType>): ValueClass<ComplexType>

    fun callParentGenericValueClass(result: ValueClass<T>): ValueClass<T>

    fun <R : T> callGenericValueClass(result: ValueClass<R>): ValueClass<R>
}
