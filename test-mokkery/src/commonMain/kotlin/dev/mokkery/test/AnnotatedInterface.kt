package dev.mokkery.test

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
annotation class AnnotationA

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
annotation class AnnotationB

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
annotation class AnnotationC

interface AnnotatedInterface {

    @AnnotationA
    fun annotatedA()

    @AnnotationB
    fun annotatedB()

    @AnnotationC
    fun annotatedC()

    fun typeAnnotatedA(input: @AnnotationA String): @AnnotationA String

    fun typeAnnotatedB(input: @AnnotationB String): @AnnotationB String
}
