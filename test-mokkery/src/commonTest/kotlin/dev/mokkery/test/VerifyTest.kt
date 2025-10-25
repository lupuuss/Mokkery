package dev.mokkery.test

import dev.mokkery.MockMode.autofill
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.atLeast
import dev.mokkery.verify.VerifyMode.Companion.atMost
import dev.mokkery.verify.VerifyMode.Companion.exhaustive
import dev.mokkery.verify.VerifyMode.Companion.exhaustiveOrder
import dev.mokkery.verify.VerifyMode.Companion.inRange
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verify.VerifyMode.Companion.order
import kotlin.test.Test

class VerifyTest {

    private val mock = mock<RegularMethodsInterface>(autofill)

    @Test
    fun testDetectsMissingCallsSoft() {
        mock.callPrimitive(2)
        mock.callPrimitive(1)
        assertVerified {
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
        assertVerified { verify(atLeast(3)) { mock.callPrimitive(1) } }
        verify(atLeast(2)) { mock.callPrimitive(1) }
    }

    @Test
    fun testDetectsMissingCallsAtMost() {
        mock.callPrimitive(1)
        mock.callPrimitive(1)
        assertVerified { verify(atMost(1)) { mock.callPrimitive(1) } }
        verify(atMost(2)) { mock.callPrimitive(1) }
    }


    @Test
    fun testDetectsMissingCallsInRange() {
        mock.callPrimitive(1)
        mock.callPrimitive(1)
        assertVerified { verify(inRange(3..4)) { mock.callPrimitive(1) } }
        verify(inRange(2..3)) { mock.callPrimitive(1) }
    }

    @Test
    fun testDetectsMissingCallsOrder() {
        mock.callPrimitive(1)
        mock.callPrimitive(2)
        assertVerified {
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
        assertVerified {
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
        assertVerified {
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
        assertVerified { verify(not) { mock.callPrimitive(1) } }
        verify(not) { mock.callPrimitive(2) }
    }

    @Test
    fun testDetectsChangedOrderOfCallsOrder() {
        mock.callPrimitive(3)
        mock.callPrimitive(2)
        mock.callPrimitive(1)
        assertVerified {
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
        assertVerified {
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
        assertVerified {
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
        assertVerified {
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
