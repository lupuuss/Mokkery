package dev.mokkery.test

@Retention(AnnotationRetention.RUNTIME)
annotation class AnnotationA

@Retention(AnnotationRetention.RUNTIME)
annotation class AnnotationB

@Retention(AnnotationRetention.RUNTIME)
annotation class AnnotationC

interface AnnotatedInterface {

    @AnnotationA
    fun annotatedA()

    @AnnotationB
    fun annotatedB()

    @AnnotationC
    fun annotatedC()
}
