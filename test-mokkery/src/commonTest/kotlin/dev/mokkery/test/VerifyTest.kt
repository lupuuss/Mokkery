package dev.mokkery.test

import dev.mokkery.MockMode.autofill
import dev.mokkery.MokkeryScope
import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.internal.MokkeryInternals
import dev.mokkery.internal.mokkeryInternals
import dev.mokkery.internal.resetMocksCounter
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.atLeast
import dev.mokkery.verify.VerifyMode.Companion.atMost
import dev.mokkery.verify.VerifyMode.Companion.exhaustive
import dev.mokkery.verify.VerifyMode.Companion.exhaustiveOrder
import dev.mokkery.verify.VerifyMode.Companion.inRange
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verify.VerifyMode.Companion.order
import kotlin.test.BeforeTest
import kotlin.test.Test

class VerifyTest {

    init {
        @OptIn(InternalMokkeryApi::class)
        MokkeryScope
            .global
            .mokkeryInternals
            .resetMocksCounter()
    }

    private val mock = mock<RegularMethodsInterface>(autofill)

    @Test
    fun testDetectsMissingCallsSoft() {
        mock.callPrimitive(2)
        mock.callPrimitive(1)
        assertVerifiedWith(
            """
                Expected any call, but no matching calls for RegularMethodsInterface(1).callPrimitive(input = 3)!
                Results for RegularMethodsInterface(1):
                # Calls to the same method with failing matchers:
                  RegularMethodsInterface(1).callPrimitive(input = 2)
                    [-] input:
                       expect: 3
                       actual: 2
                  RegularMethodsInterface(1).callPrimitive(input = 1)
                    [-] input:
                       expect: 3
                       actual: 1

            """.trimIndent()
        ) {
            verify {
                mock.callPrimitive(1)
                mock.callPrimitive(2)
                mock.callPrimitive(3)
            }
        }
        verify {
            mock.callPrimitive(1)
            mock.callPrimitive(2)
        }
    }

    @Test
    fun testDetectsMissingCallsAtLeast() {
        mock.callPrimitive(1)
        mock.callPrimitive(1)
        assertVerifiedWith(
            """
                Expected at least 3 calls, but 2 occurred for RegularMethodsInterface(1).callPrimitive(input = 1)!
                Results for RegularMethodsInterface(1):
                # Matching calls:
                  RegularMethodsInterface(1).callPrimitive(input = 1)
                  RegularMethodsInterface(1).callPrimitive(input = 1)

            """.trimIndent()
        ) {
            verify(atLeast(3)) { mock.callPrimitive(1) }
        }
        verify(atLeast(2)) { mock.callPrimitive(1) }
    }

    @Test
    fun testDetectsMissingCallsAtMost() {
        mock.callPrimitive(1)
        mock.callPrimitive(1)
        assertVerifiedWith(
            """
                Expected exactly 1 calls, but 2 occurred for RegularMethodsInterface(1).callPrimitive(input = 1)!
                Results for RegularMethodsInterface(1):
                # Matching calls:
                  RegularMethodsInterface(1).callPrimitive(input = 1)
                  RegularMethodsInterface(1).callPrimitive(input = 1)

            """.trimIndent()
        ) {
            verify(atMost(1)) { mock.callPrimitive(1) }
        }
        verify(atMost(2)) { mock.callPrimitive(1) }
    }


    @Test
    fun testDetectsMissingCallsInRange() {
        mock.callPrimitive(1)
        mock.callPrimitive(1)
        assertVerifiedWith(
            """
                Expected calls count to be in range 3..4, but 2 occurred for RegularMethodsInterface(1).callPrimitive(input = 1)!
                Results for RegularMethodsInterface(1):
                # Matching calls:
                  RegularMethodsInterface(1).callPrimitive(input = 1)
                  RegularMethodsInterface(1).callPrimitive(input = 1)

            """.trimIndent()
        ) {
            verify(inRange(3..4)) { mock.callPrimitive(1) }
        }
        verify(inRange(2..3)) { mock.callPrimitive(1) }
    }

    @Test
    fun testDetectsMissingCallsOrder() {
        mock.callPrimitive(1)
        mock.callPrimitive(2)
        assertVerifiedWith(
            """
                Expected calls in specified order but not satisfied! Failed at 3. RegularMethodsInterface(1).callPrimitive(input = 3)!
                Expected calls with matches (x.) and unverified calls (*) in order:
                1. ┌ RegularMethodsInterface(1).callPrimitive(input = 1)
                   └ RegularMethodsInterface(1).callPrimitive(input = 1)
                2. ┌ RegularMethodsInterface(1).callPrimitive(input = 2)
                   └ RegularMethodsInterface(1).callPrimitive(input = 2)
                3. ┌ RegularMethodsInterface(1).callPrimitive(input = 3)
                   └ No matching call!

            """.trimIndent()
        ) {
            verify(order) {
                mock.callPrimitive(1)
                mock.callPrimitive(2)
                mock.callPrimitive(3)
            }
        }
        verify(order) {
            mock.callPrimitive(1)
            mock.callPrimitive(2)
        }
    }

    @Test
    fun testDetectsMissingCallsExhaustiveOrder() {
        mock.callPrimitive(1)
        mock.callPrimitive(2)
        assertVerifiedWith(
            """
                Expected strict order of calls without unverified ones, but not satisfied!
                Expected calls with matches (x.) and unverified calls (*) in order:
                1. ┌ RegularMethodsInterface(1).callPrimitive(input = 1)
                   └ RegularMethodsInterface(1).callPrimitive(input = 1)
                2. ┌ RegularMethodsInterface(1).callPrimitive(input = 2)
                   └ RegularMethodsInterface(1).callPrimitive(input = 2)
                3. ┌ RegularMethodsInterface(1).callPrimitive(input = 3)
                   └ No matching call!

            """.trimIndent()
        ) {
            verify(exhaustiveOrder) {
                mock.callPrimitive(1)
                mock.callPrimitive(2)
                mock.callPrimitive(3)
            }
        }
        verify(exhaustiveOrder) {
            mock.callPrimitive(1)
            mock.callPrimitive(2)
        }
    }

    @Test
    fun testDetectsMissingCallsExhaustive() {
        mock.callPrimitive(1)
        mock.callPrimitive(2)
        assertVerifiedWith(
            """
                No matching call for RegularMethodsInterface(1).callPrimitive(input = 3)!
                Results for RegularMethodsInterface(1):
                # Calls to the same method with failing matchers:
                  RegularMethodsInterface(1).callPrimitive(input = 1)
                    [-] input:
                       expect: 3
                       actual: 1
                  RegularMethodsInterface(1).callPrimitive(input = 2)
                    [-] input:
                       expect: 3
                       actual: 2

            """.trimIndent()
        ) {
            verify(exhaustive) {
                mock.callPrimitive(1)
                mock.callPrimitive(2)
                mock.callPrimitive(3)
            }
        }
        verify(exhaustive) {
            mock.callPrimitive(1)
            mock.callPrimitive(2)
        }
    }

    @Test
    fun testDetectsPresentCallsNot() {
        mock.callPrimitive(1)
        assertVerifiedWith(
            """
                Calls to RegularMethodsInterface(1).callPrimitive(input = 1) were not expected, but occurred:
                * RegularMethodsInterface(1).callPrimitive(input = 1)

            """.trimIndent()
        ) {
            verify(not) { mock.callPrimitive(1) }
        }
        verify(not) { mock.callPrimitive(2) }
    }

    @Test
    fun testDetectsChangedOrderOfCallsOrder() {
        mock.callPrimitive(3)
        mock.callPrimitive(2)
        mock.callPrimitive(1)
        assertVerifiedWith(
            """
                Expected calls in specified order but not satisfied! Failed at 2. RegularMethodsInterface(1).callPrimitive(input = 2)!
                Expected calls with matches (x.) and unverified calls (*) in order:
                *    RegularMethodsInterface(1).callPrimitive(input = 3)
                *    RegularMethodsInterface(1).callPrimitive(input = 2)
                1. ┌ RegularMethodsInterface(1).callPrimitive(input = 1)
                   └ RegularMethodsInterface(1).callPrimitive(input = 1)
                2. ┌ RegularMethodsInterface(1).callPrimitive(input = 2)
                   └ No matching call!

            """.trimIndent()
        ) {
            verify(order) {
                mock.callPrimitive(1)
                mock.callPrimitive(2)
            }
        }
        verify(order) {
            mock.callPrimitive(2)
            mock.callPrimitive(1)
        }
    }

    @Test
    fun testDetectsChangedOrderOfCallsExhaustiveOrder() {
        mock.callPrimitive(2)
        mock.callPrimitive(1)
        assertVerifiedWith(
            """
                Expected strict order of calls without unverified ones, but not satisfied!
                Expected calls with matches (x.) and unverified calls (*) in order:
                *    RegularMethodsInterface(1).callPrimitive(input = 2)
                1. ┌ RegularMethodsInterface(1).callPrimitive(input = 1)
                   └ RegularMethodsInterface(1).callPrimitive(input = 1)
                2. ┌ RegularMethodsInterface(1).callPrimitive(input = 2)
                   └ No matching call!

            """.trimIndent()
        ) {
            verify(exhaustiveOrder) {
                mock.callPrimitive(1)
                mock.callPrimitive(2)
            }
        }
        verify(exhaustiveOrder) {
            mock.callPrimitive(2)
            mock.callPrimitive(1)
        }
    }


    @Test
    fun testDetectsExtraCallsInExhaustive() {
        mock.callPrimitive(3)
        mock.callPrimitive(2)
        mock.callPrimitive(1)
        assertVerifiedWith(
            """
                All expected calls have been satisfied! However, there should not be any unverified calls, yet these are present:
                * RegularMethodsInterface(1).callPrimitive(input = 3)

            """.trimIndent()
        ) {
            verify(exhaustive) {
                mock.callPrimitive(1)
                mock.callPrimitive(2)
            }
        }
        verify(exhaustive) {
            mock.callPrimitive(2)
            mock.callPrimitive(1)
            mock.callPrimitive(3)
        }
    }

    @Test
    fun testDetectsExtraCallsInExhaustiveOrder() {
        mock.callPrimitive(3)
        mock.callPrimitive(2)
        mock.callPrimitive(1)
        assertVerifiedWith(
            """
                Expected strict order of calls without unverified ones, but not satisfied!
                Expected calls with matches (x.) and unverified calls (*) in order:
                1. ┌ RegularMethodsInterface(1).callPrimitive(input = 3)
                   └ RegularMethodsInterface(1).callPrimitive(input = 3)
                2. ┌ RegularMethodsInterface(1).callPrimitive(input = 2)
                   └ RegularMethodsInterface(1).callPrimitive(input = 2)
                3. ┌ RegularMethodsInterface(1).callPrimitive(input = 1)
                   └ RegularMethodsInterface(1).callPrimitive(input = 1)
                4. ┌ RegularMethodsInterface(1).callPrimitive(input = 0)
                   └ No matching call!

            """.trimIndent()
        ) {
            verify(exhaustiveOrder) {
                mock.callPrimitive(3)
                mock.callPrimitive(2)
                mock.callPrimitive(1)
                mock.callPrimitive(0)
            }
        }
        verify(exhaustiveOrder) {
            mock.callPrimitive(3)
            mock.callPrimitive(2)
            mock.callPrimitive(1)
        }
    }


}
