package dev.mokkery.test

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.returns
import dev.mokkery.answering.returnsArgAt
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.matcher.logical.or
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.not
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TemplatingTest {

    private val mock = mock<RegularMethodsInterface>()

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
        assertFailsWithResultAccessError {
            verify {
                val variable = mock.callPrimitive(0)
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultInWhen() {
        assertFailsWithResultAccessError {
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
        assertFailsWithResultAccessError {
            verify {
                fun nested() = mock.callPrimitive(1)
                nested()
            }
        }
    }


    @Test
    fun testFailsWhenWrappingMockCallInScopeFunction() {
        assertFailsWithResultAccessError {
            every {
                1.let { mock.callPrimitive(it) }
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultInAnotherCall() {
        assertFailsWithResultAccessError {
            verify {
                listOf(mock.callPrimitive(1))
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultInCondition() {
        assertFailsWithResultAccessError {
            verify {
                if (mock.callPrimitive(1) == 1) return@verify
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultAsIfCondition() {
        assertFailsWithResultAccessError {
            verify {
                if (mock.callBoolean(false)) return@verify
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultAsLoopCondition() {
        assertFailsWithResultAccessError {
            verify {
                while (mock.callBoolean(false)) return@verify
            }
        }
    }

    @Test
    fun testFailsWhenPassingMockCallResultToOtherMock() {
        assertFailsWithResultAccessError {
            verify {
                mock.callPrimitive(mock.callPrimitive(1))
            }
        }
    }


    @Test
    fun testFailsWhenPassingNestedMockCallResultToOtherMethodCall() {
        assertFailsWithResultAccessError {
            val list = listOf<Int>()
            verify {
                mock.callPrimitive(list.getOrElse(mock.callPrimitive(1)) { 0 })
            }
        }
    }

    @Test
    fun testFailsWhenPassingMockCallResultToMethodCall() {
        assertFailsWithResultAccessError {
            val list = listOf<Int>()
            verify {
                list[mock.callPrimitive(1)]
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

    private fun assertFailsWithResultAccessError(block: () -> Unit) {
        val error = assertFailsWith<MokkeryRuntimeException> { block() }
        assertEquals(mockResultAccessError, error.message)
    }

    private interface SelfType {

        fun callWithSelf(self: SelfType): SelfType

        fun callWithListSelf(self: List<SelfType>): List<SelfType>
    }

    private val mockResultAccessError = "The result of a mock method must not be accessed inside `every` or `verify`." +
                " If you're trying to invoke a method with an extension receiver or context parameters," +
                " use the `dev.mokkery.templating.ext` or `dev.mokkery.templating.ctx` functions instead."
}
