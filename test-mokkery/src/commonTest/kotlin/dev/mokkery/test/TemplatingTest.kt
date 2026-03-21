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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(InternalMokkeryApi::class)
class TemplatingTest {

    private val mock = mock<RegularMethodsInterface>()

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
    fun testUnwrapping() {
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
        assertFailsWithResultAccessError(mock, "callPrimitive") {
            verify {
                val variable = mock.callPrimitive(0)
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultInWhen() {
        assertFailsWithResultAccessError(mock, "callPrimitive") {
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
        assertFailsWithResultAccessError(mock, "callPrimitive") {
            verify {
                fun nested() = mock.callPrimitive(1)
                nested()
            }
        }
    }


    @Test
    fun testFailsWhenWrappingMockCallInScopeFunction() {
        assertFailsWithResultAccessError(mock, "callPrimitive") {
            every {
                1.let { mock.callPrimitive(it) }
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultInAnotherCall() {
        assertFailsWithResultAccessError(mock, "callPrimitive") {
            verify {
                listOf(mock.callPrimitive(1))
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultInCondition() {
        assertFailsWithResultAccessError(mock, "callPrimitive") {
            verify {
                if (mock.callPrimitive(1) == 1) return@verify
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultAsIfCondition() {
        assertFailsWithResultAccessError(mock, "callBoolean") {
            verify {
                if (mock.callBoolean(false)) return@verify
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultAsLoopCondition() {
        assertFailsWithResultAccessError(mock, "callBoolean") {
            verify {
                while (mock.callBoolean(false)) return@verify
            }
        }
    }

    @Test
    fun testFailsWhenPassingMockCallResultToOtherMock() {
        assertFailsWithResultAccessError(mock, "callPrimitive") {
            verify {
                mock.callPrimitive(mock.callPrimitive(1))
            }
        }
    }


    @Test
    fun testFailsWhenPassingNestedMockCallResultToOtherMethodCall() {
        assertFailsWithResultAccessError(mock, "callPrimitive") {
            val list = listOf<Int>()
            verify {
                mock.callPrimitive(list.getOrElse(mock.callPrimitive(1)) { 0 })
            }
        }
    }

    @Test
    fun testFailsWhenPassingMockCallResultToMethodCall() {
        assertFailsWithResultAccessError(mock, "callPrimitive") {
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
        verify(not) {
            mock.callPrimitive(1)
            println(this)
            mock.callPrimitive(any())
            println(this)
            mock.callPrimitive(2)
            println(this)
        }
    }

    private fun assertFailsWithResultAccessError(obj: Any, functionName: String, block: () -> Unit) {
        assertMokkeryError(
            expectedMessage = """
                The result of calling `$functionName` on $obj must not be accessed inside `every` or `verify`.
                
                If you're trying to mock a member function with an extension receiver or context parameters, use `dev.mokkery.templating.ext` or `dev.mokkery.templating.ctx` instead of Kotlin scope functions (e.g. `let`, `run`). 
                Otherwise, using scope functions here is not supported.
            """.trimIndent(),
            block = block
        )
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
