package dev.mokkery.annotations

@MustBeDocumented
@Retention(value = AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPEALIAS, AnnotationTarget.PROPERTY)
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR, message = "This is a delicate Mokkery API and its use requires care. " +
            "Make sure you fully read and understand documentation of the declaration that is marked as a delicate API."
)
public annotation class DelicateMokkeryApi

@MustBeDocumented
@Retention(value = AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPEALIAS, AnnotationTarget.PROPERTY)
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR, message = "This is an internal Mokkery API and it should not be used!"
)
public annotation class InternalMokkeryApi
