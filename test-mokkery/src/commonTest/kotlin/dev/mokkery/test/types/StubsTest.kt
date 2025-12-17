package dev.mokkery.test.types

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.test.ComplexType
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration

class StubsTest {

    private val mock = mock<TypeToMock<Int>>()

    @Test
    fun test() {
        every { mock.foo() } returns 1
        assertEquals(1, mock.foo())
    }
}

private enum class EnumToStub {
    A, B, C
}

@Suppress("ArrayInDataClass")
private data class DataClassToStub<T : Number>(
    val short: Short,
    val long: Long,
    val char: Char,
    val boolean: Boolean,
    val func: (Int) -> ComplexType,
    val genericFunc: (Int) -> T,
    val suspendFunc: suspend (Int) -> ComplexType,
    val klass: KClass<Int>,
    val list: List<Int>,
    val iterable: Iterable<ComplexType>,
    val map: Map<String, ComplexType>,
    val mutableMap: MutableMap<String, ComplexType>,
    val classDefaultCtor: ClassDefaultCtorToStub,
    val classCtor: ClassCtorToStub,
    val nestedStubs: ClassNestedStubs,
    val interfaceToStub: InterfaceToStub,
    val abstractToStub: AbstractToStub,
    val generic: T,
    val duration: Duration,
    val longArray: LongArray,
    val genericArray: Array<T>,
    val typedArray: Array<String>,
    val starArray: Array<*>,
    val genericClassWithUpperBound: GenericClassWithUpperBound<T>,
    val genericClassWithUpperBoundStar: GenericClassWithUpperBound<*>,
    val range: IntRange,
    val throwable: Throwable,
    val sequence: Sequence<T>,
    val enumToStub: EnumToStub,
) {
    init {
        func(1)
        interfaceToStub.accept(this).type.i
    }
}

private class ClassDefaultCtorToStub

private class ClassCtorToStub(val i: Int)

private class ClassNestedStubs(
    val complexType: ComplexType,
    val type: ClassCtorToStub
)

private class GenericClassWithUpperBound<out T : Number>(val value: T)

private abstract class AbstractToStub(i: Int, array: Array<String>, ctorToStub: ClassCtorToStub)

private interface InterfaceToStub {

    fun accept(stubbed: DataClassToStub<*>): ClassNestedStubs
}

private abstract class TypeToMock<T : Number>(toBeStubbed: DataClassToStub<T>) {

    abstract fun foo(): Int
}
