package dev.mokkery.test

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.MokkeryScope
import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.answering.returns
import dev.mokkery.answering.returnsArgAt
import dev.mokkery.every
import dev.mokkery.internal.mokkeryInternals
import dev.mokkery.internal.resetMocksCounter
import dev.mokkery.matcher.any
import dev.mokkery.matcher.logical.or
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.not
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(InternalMokkeryApi::class)
class TemplatingTest {

    @BeforeTest
    fun before() {
        MokkeryScope.global
            .mokkeryInternals
            .resetMocksCounter()
    }

    @Test
    fun testEmptyEveryCall() {
        assertFailsWithSingleEveryCallButNoCallsAtAll {
            every { }
        }
    }

    @Test
    fun testEveryWithNonMockCall() {
        assertFailsWithSingleEveryCallButNoCallsAtAll {
            val list = listOf<Int>()
            every { list.size }
        }
    }

    @Test
    fun testEveryWithMoreThanOneCall() {
        MokkeryScope.global.mokkeryInternals.resetMocksCounter()
        assertMokkeryError(
            """
                Each 'every' block requires exactly one call to a mock, but there are more calls than expected:
                1. RegularMethodsInterface(1).callPrimitive(input = any())
                2. RegularMethodsInterface(1).callComplex(input = any())
                
            """.trimIndent()
        ) {
            val mock = mock<RegularMethodsInterface>()
            every {
                mock.callPrimitive(any())
                mock.callComplex(any())
            }
        }
    }

    @Test
    fun testEmptyVerifyCall() {
        assertFailsWithEmptyVerifyBlock {
            verify { }
        }
    }

    @Test
    fun testVerifyWithNonMockCall() {
        assertFailsWithEmptyVerifyBlock {
            val list = listOf<Int>()
            verify { list.size }
        }
    }

    @Test
    fun testFinalMockCallInEveryBlock() {
        val mock = mock<AbstractClassLevel1>()
        assertFailsWithMockFinalCall("AbstractClassLevel1(1)", "finalMethod") {
            every { mock.finalMethod() }
        }
    }

    @Test
    fun testFinalMockCallInVerifyBlock() {
        val mock = mock<AbstractClassLevel1>()
        assertFailsWithMockFinalCall("AbstractClassLevel1(1)", "finalMethod") {
            verify { mock.finalMethod() }
        }
    }

    @Test
    fun testInlineMockCallInEveryBlock() {
        val mock = mock<AbstractClassLevel1>()
        assertFailsWithMockFinalCall("AbstractClassLevel1(1)", "inlineMethod") {
            every { mock.inlineMethod() }
        }
    }

    @Test
    fun testInlineMockCallInVerifyBlock() {
        val mock = mock<AbstractClassLevel1>()
        assertFailsWithMockFinalCall("AbstractClassLevel1(1)", "inlineMethod") {
            verify { mock.inlineMethod() }
        }
    }

    @Test
    fun testInlinePropertyMockCallInEveryBlock() {
        val mock = mock<AbstractClassLevel1>()
        assertFailsWithMockFinalCall("AbstractClassLevel1(1)", "get inlineProperty") {
            every { mock.inlineProperty }
        }
    }

    @Test
    fun testInlinePropertyMockCallInVerifyBlock() {
        val mock = mock<AbstractClassLevel1>()
        assertFailsWithMockFinalCall("AbstractClassLevel1(1)", "set inlineProperty") {
            verify { mock.inlineProperty = any() }
        }
    }

    @Test
    fun testUnwrapping() {
        val mock = mock<RegularMethodsInterface>()
        val mocks = listOf(mock)
        every { mocks[0].callPrimitive(or(1, 2, 3)) } returnsArgAt 0
        assertEquals(1, mocks[0].callPrimitive(1))
        assertEquals(2, mocks[0].callPrimitive(2))
        assertEquals(3, mocks[0].callPrimitive(3))
        assertFailsWith<MokkeryRuntimeException> { mocks[0].callPrimitive(4) }
        verify {
            mocks[0].callPrimitive(1)
            mocks[0].callPrimitive(2)
            mocks[0].callPrimitive(3)
        }
    }

    @Test
    fun testFailsAccessingMockCallResultInVariable() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            verify {
                val variable = mock.callPrimitive(0)
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultInWhen() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            verify {
                when (true) {
                    true -> mock.callPrimitive(1)
                    else -> Unit
                }
            }
        }
    }


    @Test
    fun testFailsWhenAccessingMockCallResultInNestedFunction() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            verify {
                fun nested() = mock.callPrimitive(1)
                nested()
            }
        }
    }


    @Test
    fun testFailsWhenWrappingMockCallInScopeFunction() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            every {
                1.let { mock.callPrimitive(it) }
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultInAnotherCall() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            verify {
                listOf(mock.callPrimitive(1))
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultInCondition() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            verify {
                if (mock.callPrimitive(1) == 1) return@verify
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultAsIfCondition() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callBoolean") {
            verify {
                if (mock.callBoolean(false)) return@verify
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultAsLoopCondition() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callBoolean") {
            verify {
                while (mock.callBoolean(false)) return@verify
            }
        }
    }

    @Test
    fun testFailsWhenPassingMockCallResultToOtherMock() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            verify {
                mock.callPrimitive(mock.callPrimitive(1))
            }
        }
    }


    @Test
    fun testFailsWhenPassingNestedMockCallResultToOtherMethodCall() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            val list = listOf<Int>()
            verify {
                mock.callPrimitive(list.getOrElse(mock.callPrimitive(1)) { 0 })
            }
        }
    }

    @Test
    fun testFailsWhenPassingMockCallResultToMethodCall() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            val list = listOf<Int>()
            verify {
                list[mock.callPrimitive(1)]
            }
        }
    }

    @Test
    fun testFailsWhenMatcherUsedWithNonMockOverridableType() {
        assertMokkeryError(
            """
                Call to `get` was expected to be performed on a mock of List type, but the receiver was not a mock - it was an instance of EmptyList type => []
            """.trimIndent()
        ) {
            val list = listOf<Int>()
            verify {
                list[any()]
            }
        }
    }

    @Test
    fun testFailsWhenMatcherUsedWithNonMockOverridableAnonymousType() {
        assertMokkeryError(
            """
                Call to `compareTo` was expected to be performed on a mock of Comparable type, but the receiver was not a mock - it was an instance of anonymous type => comparable-to-string
            """.trimIndent()
        ) {
            val comparable: Comparable<Int> = object : Comparable<Int> {
                override fun compareTo(other: Int): Int = other

                override fun toString(): String = "comparable-to-string"
            }
            verify {
                comparable.compareTo(any())
            }
        }
    }

    @Test
    fun testSelf() {
        val mock = mock<SelfType>()
        every { mock.callWithSelf(mock) } returns mock
        every { mock.callWithListSelf(listOf(mock)) } returns listOf(mock)
        assertEquals(mock, mock.callWithSelf(mock))
        assertEquals(listOf(mock), mock.callWithListSelf(listOf(mock)))
    }

    @Test
    fun testArgMatchersScopeAccess() {
        val mock = mock<RegularMethodsInterface>()
        verify(not) {
            mock.callPrimitive(1)
            println(this)
            mock.callPrimitive(any())
            println(this)
            mock.callPrimitive(2)
            println(this)
        }
    }

    private fun assertFailsWithResultAccessError(receiver: String, functionName: String, block: () -> Unit) {
        assertMokkeryError(
            expectedMessage = """
                The result of calling `$functionName` on $receiver must not be accessed inside `every` or `verify`.
                
                If you're trying to mock a member function with an extension receiver or context parameters, use `dev.mokkery.templating.ext` or `dev.mokkery.templating.ctx` instead of Kotlin scope functions (e.g. `let`, `run`). 
                Otherwise, using scope functions here is not supported.
            """.trimIndent(),
            block = block
        )
    }

    private fun assertFailsWithMockFinalCall(receiver: String, functionName: String, block: () -> Unit) {
        assertMokkeryError(
            """
                `$functionName` is final and cannot be mocked on $receiver.
                Only non-final member functions can be intercepted inside `every` or `verify`.
            """.trimIndent()
        ) {
            block()
        }
    }

    private fun assertFailsWithSingleEveryCallButNoCallsAtAll(block: () -> Unit) {
        assertMokkeryError(
            expectedMessage = """
                Each 'every' block requires exactly one call to a mock, but there are no calls to any mock!
                
                Possible reasons:
                * You are calling an object that is not a mock.
                * You are calling a mock, but the member function is final.
                * You are calling a mock, but it's an extension function instead of a member function.
                
            """.trimIndent(),
            block = block
        )
    }

    private fun assertFailsWithEmptyVerifyBlock(block: () -> Unit) {
        assertMokkeryError(
            expectedMessage = """
                Given 'verify' block does not contain any call to a mock. It's very suspicious and most probably caused by misuse.
                
                Possible reasons:
                * You are calling an object that is not a mock.
                * You are calling a mock, but the member function is final.
                * You are calling a mock, but it's an extension function instead of a member function.
            """.trimIndent(),
            block = block
        )
    }

    private interface SelfType {

        fun callWithSelf(self: SelfType): SelfType

        fun callWithListSelf(self: List<SelfType>): List<SelfType>
    }

}
