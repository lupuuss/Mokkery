import dev.mokkery.MockMode
import dev.mokkery.MokkerySuiteScope
import dev.mokkery.factory.mockFactoryOf
import dev.mokkery.factory.defaultMockMode
import kotlin.reflect.KClass

interface Service
interface Repository

val topLevelClass: KClass<Service> = Service::class

fun test(cls: KClass<Repository>, instance: Service) {
    mockFactoryOf(Service::class, Repository::class)
    mockFactoryOf(Service::class) { defaultMockMode = MockMode.autofill }
    mockFactoryOf(<!NOT_A_CLASS_LITERAL!>cls<!>)
    mockFactoryOf(Service::class, <!NOT_A_CLASS_LITERAL!>topLevelClass<!>)
    mockFactoryOf(<!NOT_A_CLASS_LITERAL!>*arrayOf(Service::class)<!>)
    mockFactoryOf(<!NOT_A_CLASS_LITERAL!>instance::class<!>)
    mockFactoryOf(Service::class, <!NOT_A_CLASS_LITERAL!>instance::class<!>)
}

fun MokkerySuiteScope.testWithScope(cls: KClass<Repository>) {
    mockFactoryOf(Service::class)
    mockFactoryOf(<!NOT_A_CLASS_LITERAL!>cls<!>)
}
