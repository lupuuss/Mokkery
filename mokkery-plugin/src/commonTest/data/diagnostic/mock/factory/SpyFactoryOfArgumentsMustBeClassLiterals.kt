import dev.mokkery.MokkerySuiteScope
import dev.mokkery.factory.spyFactoryOf
import kotlin.reflect.KClass

interface Service
interface Repository

val topLevelClass: KClass<Service> = Service::class

fun test(cls: KClass<Repository>, instance: Service) {
    spyFactoryOf(Service::class, Repository::class)
    spyFactoryOf(<!NOT_A_CLASS_LITERAL!>cls<!>)
    spyFactoryOf(Service::class, <!NOT_A_CLASS_LITERAL!>topLevelClass<!>)
    spyFactoryOf(<!NOT_A_CLASS_LITERAL!>*arrayOf(Service::class)<!>)
    spyFactoryOf(<!NOT_A_CLASS_LITERAL!>instance::class<!>)
    spyFactoryOf(Service::class, <!NOT_A_CLASS_LITERAL!>instance::class<!>)
}

fun MokkerySuiteScope.testWithScope(cls: KClass<Repository>) {
    spyFactoryOf(Service::class)
    spyFactoryOf(<!NOT_A_CLASS_LITERAL!>cls<!>)
}
