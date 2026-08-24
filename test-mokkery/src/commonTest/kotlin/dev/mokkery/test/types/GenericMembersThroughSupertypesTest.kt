package dev.mokkery.test.types

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.mockMany
import dev.mokkery.t1
import dev.mokkery.t2
import dev.mokkery.test.GenericFunctionsInterface
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GenericMembersThroughSupertypesTest {

    @Test
    fun testTemplatesGenericMemberThroughSupertypeOfStdlibType() {
        val instance = mock<List<String>>()
        val collection: Collection<String> = instance
        every { collection.contains(any()) } returns true
        assertEquals(true, instance.contains("x"))
        verify { collection.contains(any()) }
    }

    @Test
    fun testTemplatesGenericMemberThroughSupertypeWhenSubstitutedBySubtype() {
        val instance = mock<StringConsumer>()
        val consumer: Consumer<String> = instance
        every { consumer.consume(any()) } returns "ok"
        assertEquals("ok", instance.consume("x"))
        verify { consumer.consume(any()) }
    }

    @Test
    fun testTemplatesGenericMemberWhenSupertypeReordersTypeArguments() {
        val instance = mock<SwappedSub<Int, String>>()
        val base: SwappedBase<String, Int> = instance
        every { base.first(any()) } returns "first"
        every { base.second(any()) } returns "second"
        assertEquals("first", instance.first("a"))
        assertEquals("second", instance.second(1))
    }

    @Test
    fun testTemplatesGenericMemberThroughSupertypeOfMockMany() {
        val instance = mockMany<Unrelated, StringConsumer>()
        val consumer: Consumer<String> = instance.t2
        every { consumer.consume(any()) } returns "ok"
        every { instance.t1.other() } returns 1
        assertEquals("ok", instance.t2.consume("x"))
        assertEquals(1, instance.t1.other())
    }

    @Test
    fun testTemplatesMemberThroughEitherRootDeclarationOfDiamond() {
        val instance = mock<DiamondSub>()
        val left: StringConsumer = instance
        val right: RawStringConsumer = instance
        every { left.consume(any()) } returns "left"
        assertEquals("left", instance.consume("x"))
        verify { right.consume(any()) }
    }

    @Test
    fun testTemplatesGenericPropertyThroughSupertype() {
        val instance = mock<StringBox>()
        val box: Box<String> = instance
        every { box.value } returns "ok"
        every { box.value = any() } returns Unit
        assertEquals("ok", instance.value)
        instance.value = "x"
        verify {
            box.value
            box.value = "x"
        }
    }

    @Test
    fun testTemplatesGenericSuspendMemberThroughSupertype() = runTest {
        val instance = mock<StringSuspendConsumer>()
        val consumer: SuspendConsumer<String> = instance
        everySuspend { consumer.consume(any()) } returns "ok"
        assertEquals("ok", instance.consume("x"))
        verifySuspend { consumer.consume(any()) }
    }

    @Test
    fun testTemplatesMemberWithGenericReturnTypeThroughSupertype() {
        val instance = mock<StringProducer>()
        val producer: Producer<String> = instance
        every { producer.produce() } returns "ok"
        assertEquals("ok", instance.produce())
        verify { producer.produce() }
    }

    @Test
    fun testDistinguishesOverloadsOfGenericAndConcreteParameter() {
        val instance = mock<StringOverloads>()
        val base: Overloads<String> = instance
        every { base.take(any<String>()) } returns "generic"
        every { base.take(any<Int>()) } returns "concrete"
        assertEquals("generic", instance.take("x"))
        assertEquals("concrete", instance.take(1))
        verify {
            base.take(any<String>())
            base.take(any<Int>())
        }
    }

    @Test
    fun testTemplatesGenericMemberThroughSupertypeFromAnotherCompilationUnit() {
        val instance = mock<StringFunctions>()
        val base: GenericFunctionsInterface<String> = instance
        every { base.call(any()) } returns "ok"
        every { base.callGeneric<Int>(any()) } returns 1
        assertEquals("ok", instance.call("x"))
        assertEquals(1, instance.callGeneric<Int>("x"))
        verify {
            base.call(any())
            base.callGeneric<Int>(any())
        }
    }

    @Test
    fun testDistinguishesMocksOfDifferentSubtypesSharingTheSameSupertypeMember() {
        val first: Consumer<String> = mock<StringConsumer>()
        val second: Consumer<String> = mock<OtherStringConsumer>()
        every { first.consume(any()) } returns "first"
        every { second.consume(any()) } returns "second"
        assertEquals("first", first.consume("x"))
        assertEquals("second", second.consume("x"))
        verify { first.consume(any()) }
        verify { second.consume(any()) }
    }

    private interface Consumer<T> {

        fun consume(value: T): String
    }

    private interface StringConsumer : Consumer<String>

    private interface OtherStringConsumer : Consumer<String>

    private interface RawStringConsumer {

        fun consume(value: String): String
    }

    private interface DiamondSub : StringConsumer, RawStringConsumer

    private interface SwappedBase<A, B> {

        fun first(value: A): String

        fun second(value: B): String
    }

    private interface SwappedSub<X, Y> : SwappedBase<Y, X>

    private interface Unrelated {

        fun other(): Int
    }

    private interface Box<T> {

        var value: T
    }

    private interface StringBox : Box<String>

    private interface SuspendConsumer<T> {

        suspend fun consume(value: T): String
    }

    private interface StringSuspendConsumer : SuspendConsumer<String>

    private interface Producer<T> {

        fun produce(): T
    }

    private interface StringProducer : Producer<String>

    private interface Overloads<T> {

        fun take(value: T): String

        fun take(value: Int): String
    }

    private interface StringOverloads : Overloads<String>

    private interface StringFunctions : GenericFunctionsInterface<String>
}
