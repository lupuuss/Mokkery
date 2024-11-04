package dev.mokkery.test

interface GenericSuspendFunctionsInterface<Type> {

    suspend fun <R> callGeneric(input: Type): R

    suspend fun <R : Any> callGenericNullabilityMarkers(input: Type & Any): R?

    suspend fun <R : Any> callGenericNestedNullabilityMarkers(input: List<Type & Any>): List<R?>

    suspend fun <R : CharSequence> callGenericBound(input: Type): R

    suspend fun <R : Comparable<R>> callGenericBoundRecursiveParam(input: Type): R

    suspend fun <R : Type> callGenericBoundParentParam(input: Type): R

    suspend fun <R : List<Type>> callGenericBoundNestedParentParam(input: Type): R

    suspend fun <R> callGenericMultipleBounds(input: Type): R where R : CharSequence, R : Comparable<R>

    suspend fun callGenericWithStarProjection(input: List<*>): List<*>

    suspend fun <T : GenericSuspendFunctionsInterface<T>> callSelf(input: T): GenericSuspendFunctionsInterface<Type>
}
