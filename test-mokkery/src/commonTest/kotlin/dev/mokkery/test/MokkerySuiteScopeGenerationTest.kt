package dev.mokkery.test

import dev.mokkery.MokkerySuiteScope
import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.answering.returns
import dev.mokkery.answering.returnsArgAt
import dev.mokkery.every
import dev.mokkery.internal.mokkeryInternals
import dev.mokkery.internal.resetMocksCounter
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.mockMany
import dev.mokkery.mocks
import dev.mokkery.spy
import dev.mokkery.t2
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exhaustiveOrder
import dev.mokkery.verifyNoMoreCalls
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(InternalMokkeryApi::class)
class MokkerySuiteScopeGenerationTest : MokkerySuiteScope {

    @BeforeTest
    fun before() {
        mokkeryInternals.resetMocksCounter()
    }

    @Test
    fun testVerifyExhaustivenessInScope() {
        val mockA = mock<RegularMethodsInterface> {
            every { callPrimitive(any()) } returnsArgAt 0
        }
        val mockB= mock<RegularMethodsInterface> {
            every { callPrimitive(any()) } returnsArgAt 0
        }
        mockA.callPrimitive(1)
        mockB.callPrimitive(2)
        assertVerifiedWith(
            """
            Expected strict order of calls without unverified ones, but not satisfied!
            Expected calls with matches (x.) and unverified calls (*) in order:
            1. ┌ RegularMethodsInterface(1).callPrimitive(input = 1)
               └ RegularMethodsInterface(1).callPrimitive(input = 1)
            *    RegularMethodsInterface(2).callPrimitive(input = 2)
            
            """.trimIndent()
        ) {
            verify(exhaustiveOrder) {
                mockA.callPrimitive(1)
            }
        }
    }

    @Test
    fun testVerifyNoMoreCallsDetectsUnverifiedCallsInScope() {
        val mockA = mock<RegularMethodsInterface> {
            every { callPrimitive(any()) } returnsArgAt 0
        }
        val mockB= mock<RegularMethodsInterface> {
            every { callPrimitive(any()) } returnsArgAt 0
        }
        mockA.callPrimitive(1)
        mockB.callPrimitive(2)
        assertVerifiedWith(
            """
            No unverified calls expected, but these are present:
            * RegularMethodsInterface(1).callPrimitive(input = 1)
            * RegularMethodsInterface(2).callPrimitive(input = 2)

            """.trimIndent()
        ) {
            verifyNoMoreCalls()
        }
    }

    @Test
    fun testVerifyNoMoreCallsPassesWhenAllVerifiedCallsInScope() {
        val mockA = mock<RegularMethodsInterface> {
            every { callPrimitive(any()) } returnsArgAt 0
        }
        val mockB= mock<RegularMethodsInterface> {
            every { callPrimitive(any()) } returnsArgAt 0
        }
        mockA.callPrimitive(1)
        mockB.callPrimitive(2)
        verify {
            mockA.callPrimitive(1)
            mockB.callPrimitive(2)
        }
        verifyNoMoreCalls()
    }

    @Test
    fun testVerifyNoMoreCallsPassesWhenNoCallsInScope() {
        verifyNoMoreCalls()
    }

    @Test
    fun testContainsAllMocks() {
        val mockA = mock<RegularMethodsInterface> {
            every { callPrimitive(any()) } returnsArgAt 0
        }
        val mockB= mock<RegularMethodsInterface> {
            every { callPrimitive(any()) } returnsArgAt 0
        }
        val mockC = mock<(Int) -> Int>()
        val expectedMocks = listOf(mockA, mockB, mockC)
        assertEquals(expectedMocks, mocks)
    }

    @Test
    fun testLambdas() {
        val mockA = mock<RegularMethodsInterface> {
            every { callPrimitive(any()) } returnsArgAt 0
        }
        val funMock = mock<(Int) -> Int> { every { invoke(1) } returns 1 }
        mockA.callPrimitive(1)
        funMock(1)
        assertVerifiedWith(
            """
            Expected strict order of calls without unverified ones, but not satisfied!
            Expected calls with matches (x.) and unverified calls (*) in order:
            1. ┌ RegularMethodsInterface(1).callPrimitive(input = 1)
               └ RegularMethodsInterface(1).callPrimitive(input = 1)
            *    Function1(2).invoke(p1 = 1)

            """.trimIndent()
        ) {
            verify(exhaustiveOrder) {
                mockA.callPrimitive(1)
            }
        }
    }

    @Test
    fun testSpy() {
        val mockA = mock<RegularMethodsInterface> {
            every { callPrimitive(any()) } returnsArgAt 0
        }
        val spy = spy(listOf(1, 2, 3))
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
        val mockA = mock<RegularMethodsInterface> {
            every { callPrimitive(any()) } returnsArgAt 0
        }
        val mockMany = mockMany<RegularMethodsInterface, AutoCloseable> { every { t2.close() } returns Unit }
        mockA.callPrimitive(1)
        mockMany.t2.close()
        assertVerifiedWith(
            """
            Expected strict order of calls without unverified ones, but not satisfied!
            Expected calls with matches (x.) and unverified calls (*) in order:
            1. ┌ RegularMethodsInterface(1).callPrimitive(input = 1)
               └ RegularMethodsInterface(1).callPrimitive(input = 1)
            *    MockMany2<RegularMethodsInterface, AutoCloseable>(2).close()
            
            """.trimIndent()
        ) {
            verify(exhaustiveOrder) {
                mockA.callPrimitive(1)
            }
        }
    }
}
