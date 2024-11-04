package dev.mokkery.test

interface GenericFunctionsInterface<Type> {

    fun <R> callGeneric(input: Type): R

    fun <R : Any> callGenericNullabilityMarkers(input: Type & Any): R?

    fun <R : Any> callGenericNestedNullabilityMarkers(input: List<Type & Any>): List<R?>

    fun <R : CharSequence> callGenericBound(input: Type): R

    fun <R : Comparable<R>> callGenericBoundRecursiveParam(input: Type): R

    fun <R : Type> callGenericBoundParentParam(input: Type): R

    fun <R : List<Type>> callGenericBoundNestedParentParam(input: Type): R

    fun <R> callGenericMultipleBounds(input: Type): R where R : CharSequence, R : Comparable<R>

    fun callGenericWithStarProjection(input: List<*>): List<*>

    fun <T : GenericFunctionsInterface<T>> callSelf(input: T): GenericFunctionsInterface<Type>
}
