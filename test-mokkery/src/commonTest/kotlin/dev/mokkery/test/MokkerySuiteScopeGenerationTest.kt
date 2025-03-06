package dev.mokkery.test

import dev.mokkery.MokkerySuiteScope
import dev.mokkery.answering.returns
import dev.mokkery.answering.returnsArgAt
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.mockMany
import dev.mokkery.mocks
import dev.mokkery.spy
import dev.mokkery.t2
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exhaustiveOrder
import dev.mokkery.verifyNoMoreCalls
import kotlin.test.Test
import kotlin.test.assertEquals

class MokkerySuiteScopeGenerationTest : MokkerySuiteScope {

    private val mockA = mock<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }
    private val mockB = mock<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }

    @Test
    fun testVerifyExhaustivenessInScope() {
        mockA.callPrimitive(1)
        mockB.callPrimitive(2)
        assertVerified {
            verify(exhaustiveOrder) {
                mockA.callPrimitive(1)
            }
        }
    }

    @Test
    fun testVerifyNoMoreCallsInScope() {
        mockA.callPrimitive(1)
        mockB.callPrimitive(2)
        assertVerified {
            verifyNoMoreCalls()
        }
    }

    @Test
    fun testContainsAllMocks() {
        val expectedMocks = listOf(mockA, mockB)
        assertEquals(expectedMocks, mocks)
    }

    @Test
    fun testLambdas() {
        val funMock = mock<(Int) -> Int> { every { invoke(1) } returns 1 }
        mockA.callPrimitive(1)
        funMock(1)
        assertVerified {
            verify(exhaustiveOrder) {
                mockA.callPrimitive(1)
            }
        }
    }

    @Test
    fun testSpy() {
        val spy = spy<List<Int>>(listOf(1, 2, 3))
        mockA.callPrimitive(1)
        spy[0]
        assertVerified {
            verify(exhaustiveOrder) {
                mockA.callPrimitive(1)
            }
        }
    }

    @Test
    fun testMockMany() {
        val mockMany = mockMany<RegularMethodsInterface, AutoCloseable> { every { t2.close() } returns Unit }
        mockA.callPrimitive(1)
        mockMany.t2.close()
        assertVerified {
            verify(exhaustiveOrder) {
                mockA.callPrimitive(1)
            }
        }
    }
}
