import dev.mokkery.factory.spyFactoryOf

interface Service
class FinalClass
sealed class SealedClass

fun test() {
    spyFactoryOf(<!FINAL_TYPE_CANNOT_BE_INTERCEPTED!>FinalClass<!>::class, Service::class)
    spyFactoryOf(<!SEALED_TYPE_CANNOT_BE_INTERCEPTED!>SealedClass<!>::class)
    spyFactoryOf(<!PRIMITIVE_TYPE_CANNOT_BE_INTERCEPTED!>Int<!>::class)
}
