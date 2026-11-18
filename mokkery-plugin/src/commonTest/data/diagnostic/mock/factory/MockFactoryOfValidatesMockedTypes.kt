import dev.mokkery.factory.mockFactoryOf

interface Service
class FinalClass
sealed class SealedClass

fun test() {
    mockFactoryOf(<!FINAL_TYPE_CANNOT_BE_INTERCEPTED!>FinalClass<!>::class, Service::class)
    mockFactoryOf(<!SEALED_TYPE_CANNOT_BE_INTERCEPTED!>SealedClass<!>::class)
    mockFactoryOf(<!PRIMITIVE_TYPE_CANNOT_BE_INTERCEPTED!>Int<!>::class)
}
