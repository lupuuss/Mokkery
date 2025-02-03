package dev.mokkery.test

import dev.mokkery.MokkeryTestsScope
import dev.mokkery.answering.returnsArgAt
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exhaustiveOrder
import kotlin.test.Test

abstract class BaseClass() : MokkeryTestsScope

class InheritedMokkeryTestsScopeGenerationTest : BaseClass() {

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
}
