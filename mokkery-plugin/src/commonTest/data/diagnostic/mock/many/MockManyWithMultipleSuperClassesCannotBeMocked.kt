import dev.mokkery.mockMany

abstract class AbstractClass1
abstract class AbstractClass2
abstract class AbstractClass3

fun main() {
    mockMany<AbstractClass1, <!MULTIPLE_SUPER_CLASSES_FOR_MOCK_MANY!>AbstractClass2<!>, AbstractClass3>()
}
