@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.test

interface CollectionsInterface {

    fun <T> callWithList(elements: List<T>): T
    fun <T> callWithElementsArray(elements: Array<T>): T  // array variant

    fun callWithBooleans(vararg values: Boolean): Boolean
    fun callWithBooleansArray(values: BooleanArray): Boolean

    fun callWithChars(vararg values: Char): Char
    fun callWithCharsArray(values: CharArray): Char

    fun callWithBytes(vararg values: Byte): Byte
    fun callWithBytesArray(values: ByteArray): Byte

    fun callWithShorts(vararg values: Short): Short
    fun callWithShortsArray(values: ShortArray): Short

    fun callWithInts(vararg values: Int): Int
    fun callWithIntsArray(values: IntArray): Int

    fun callWithLongs(vararg values: Long): Long
    fun callWithLongsArray(values: LongArray): Long

    fun callWithFloats(vararg values: Float): Float
    fun callWithFloatsArray(values: FloatArray): Float

    fun callWithDoubles(vararg values: Double): Double
    fun callWithDoublesArray(values: DoubleArray): Double

    fun callWithUBytes(vararg values: UByte): UByte
    fun callWithUBytesArray(values: UByteArray): UByte

    fun callWithUShorts(vararg values: UShort): UShort
    fun callWithUShortsArray(values: UShortArray): UShort

    fun callWithUInts(vararg values: UInt): UInt
    fun callWithUIntsArray(values: UIntArray): UInt

    fun callWithULongs(vararg values: ULong): ULong
    fun callWithULongsArray(values: ULongArray): ULong
}
