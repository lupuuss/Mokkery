package dev.mokkery.test.types

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.test.EnumClass
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.startCoroutineUninterceptedOrReturn
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StubsTest {

    private val mock = mock<TypeToMock<Int>>()

    @Test
    fun test() {
        every { mock.foo() } returns 1
        assertEquals(1, mock.foo())
    }
}

private abstract class TypeToMock<T : Number>(toBeStubbed: DataClassToStub<T>) {

    abstract fun foo(): Int
}

private data class DataClassToStub<T : Number>(
    val nullable: FinalPrivate?,
    val generic: T,
    val primitivesToStub: Primitives,
    val enumToStub: EnumClass,
    val lambdasToStub: Lambdas,
    val arraysToStub: Arrays,
    val collectionsToStub: Collections,
    val interfaceToStub: AutoCloseable,
    val finalClassToStub: FinalPublic,
    val openClassToStub: OpenClass,
    val abstractClassToStub: AbstractClass,
) {
    init {
        assertEquals(null, nullable)
        assertEquals(0 as Number, generic)
        primitivesToStub.assertExpected()
        assertEquals(EnumClass.A, enumToStub)
        lambdasToStub.assertExpected()
        arraysToStub.assertExpected()
        collectionsToStub.assertExpected()
        interfaceToStub.close()
        finalClassToStub.assertExpected()
        assertEquals(10, openClassToStub.function())
        assertEquals(10.0, openClassToStub.finalFunction())
        assertEquals(0, abstractClassToStub.function())
        assertEquals(10.0, abstractClassToStub.finalFunction())
    }
}

private class Primitives(
    val byte: Byte,
    val uByte: UByte,
    val short: Short,
    val uShort: UShort,
    val int: Int,
    val uInt: UInt,
    val long: Long,
    val uLong: ULong,
    val float: Float,
    val double: Double,
    val boolean: Boolean,
    val char: Char,
    val number: Number,
    val charSequence: CharSequence,
    val string: String,
    val unit: Unit,
    val kClassErased: KClass<*>,
    val kClassInt: KClass<Int>,
) {
    fun assertExpected() {
        assertEquals(0.toByte(), byte)
        assertEquals(0u.toUByte(), uByte)
        assertEquals(0.toShort(), short)
        assertEquals(0u.toUShort(), uShort)
        assertEquals(0, int)
        assertEquals(0u, uInt)
        assertEquals(0L, long)
        assertEquals(0uL, uLong)
        assertEquals(0.0f, float)
        assertEquals(0.0, double)
        assertEquals(false, boolean)
        assertEquals(0.toChar(), char)
        assertEquals(0, number)
        assertEquals("", charSequence)
        assertEquals("", string)
        assertEquals(Unit, unit)
        assertEquals(Any::class, kClassErased)
        assertEquals(Int::class, kClassInt)
    }
}

private data class Lambdas(
    val lambdaPrimitive: () -> Int,
    val lambdaUnit: (Int) -> Unit,
    val lambdaPublicFinal: (Int) -> FinalPublic,
    val lambdaPrivateFinal: () -> FinalPrivate,
    val lambdaSuspend: suspend (Int) -> FinalPublic,
) {

    fun assertExpected() {
        assertEquals(0, lambdaPrimitive())
        assertEquals(Unit, lambdaUnit(0))
        assertEquals(FinalPublic(0), lambdaPublicFinal(0))
        assertEquals(FinalPublic(0), runSuspension { lambdaSuspend(0) })
        assertFailsWith<MokkeryRuntimeException> { lambdaPrivateFinal() }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
private class Arrays(
    val anyArray: Array<Any>,
    val stringArray: Array<String>,
    val byteArray: ByteArray,
    val shortArray: ShortArray,
    val intArray: IntArray,
    val longArray: LongArray,
    val floatArray: FloatArray,
    val doubleArray: DoubleArray,
    val uByteArray: UByteArray,
    val uShortArray: UShortArray,
    val uIntArray: UIntArray,
    val uLongArray: ULongArray,
    val booleanArray: BooleanArray,
    val charArray: CharArray
) {
    fun assertExpected() {
        assertEquals(0, anyArray.size)
        assertEquals(0, stringArray.size)
        assertEquals(0, byteArray.size)
        assertEquals(0, uByteArray.size)
        assertEquals(0, shortArray.size)
        assertEquals(0, uShortArray.size)
        assertEquals(0, intArray.size)
        assertEquals(0, uIntArray.size)
        assertEquals(0, longArray.size)
        assertEquals(0, uLongArray.size)
        assertEquals(0, floatArray.size)
        assertEquals(0, doubleArray.size)
        assertEquals(0, booleanArray.size)
        assertEquals(0, charArray.size)
    }
}

private data class Collections(
    val iterable: Iterable<Int>,
    val mutableIterable: MutableIterable<Int>,
    val collection: Collection<Int>,
    val mutableCollection: MutableCollection<Int>,
    val list: List<Int>,
    val mutableList: MutableList<Int>,
    val set: Set<Int>,
    val mutableSet: MutableSet<Int>,
    val map: Map<Int, String>,
    val mutableMap: MutableMap<Int, String>
) {
    fun assertExpected() {
        assertEquals(0, iterable.count())
        assertEquals(0, mutableIterable.count())
        assertEquals(0, collection.size)
        assertEquals(0, mutableCollection.size)
        assertEquals(0, list.size)
        assertEquals(0, mutableList.size)
        assertEquals(0, set.size)
        assertEquals(0, mutableSet.size)
        assertEquals(0, map.size)
        assertEquals(0, mutableMap.size)
    }
}

private class FinalPrivate private constructor(i: Int)
private data class FinalPublic(val i: Int) {
    fun assertExpected() {
        assertEquals(0, i)
    }
}

private abstract class AbstractClass(val i: Int) {

    abstract fun function(): Int

    fun finalFunction(): Double = 10.0
}

private open class OpenClass(val i: Int) {

    open fun function(): Int = 10

    fun finalFunction(): Double = 10.0
}


@Suppress("UNCHECKED_CAST")
private fun <T> runSuspension(block: suspend () -> T): T {
    val result = block.startCoroutineUninterceptedOrReturn(Continuation(EmptyCoroutineContext) { })
    if (result == COROUTINE_SUSPENDED) {
        error("illegal suspension")
    }
    return result as T
}
