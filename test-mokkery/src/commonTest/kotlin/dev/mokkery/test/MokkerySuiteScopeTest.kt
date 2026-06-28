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

class MokkerySuiteScopeTest {

    private val scope = MokkerySuiteScope()

    private val mockA = scope.mock<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }
    private val mockB = scope.mock<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }

    @Test
    fun testVerifyExhaustivenessInScope() {
        mockA.callPrimitive(1)
        mockB.callPrimitive(2)
        assertVerified {
            scope.verify(exhaustiveOrder) {
                mockA.callPrimitive(1)
            }
        }
    }

    @Test
    fun testVerifyNoMoreCallsInScope() {
        mockA.callPrimitive(1)
        mockB.callPrimitive(2)
        assertVerified {
            scope.verifyNoMoreCalls()
        }
    }

    @Test
    fun testContainsAllMocks() {
        val mockC = scope.mock<(Int) -> Int>()
        val expectedMocks = listOf(mockA, mockB, mockC)
        assertEquals(expectedMocks, scope.mocks)
    }

    @Test
    fun testLambdas() {
        val funMock = scope.mock<(Int) -> Int> { every { invoke(1) } returns 1 }
        mockA.callPrimitive(1)
        funMock(1)
        assertVerified {
            scope.verify(exhaustiveOrder) {
                mockA.callPrimitive(1)
            }
        }
    }

    @Test
    fun testSpy() {
        val spy = scope.spy<List<Int>>(listOf(1, 2, 3))
        mockA.callPrimitive(1)
        spy[0]
        assertVerified {
            scope.verify(exhaustiveOrder) {
                mockA.callPrimitive(1)
            }
        }
    }

    @Test
    fun testMockMany() {
        val mockMany = scope.mockMany<RegularMethodsInterface, AutoCloseable> { every { t2.close() } returns Unit }
        mockA.callPrimitive(1)
        mockMany.t2.close()
        assertVerified {
            scope.verify(exhaustiveOrder) {
                mockA.callPrimitive(1)
            }
        }
    }
}
