package dev.mokkery.test

import dev.mokkery.MokkeryScope
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertNotNull

class MokkeryInstanceScopeTest {

    private val mock = mock<RegularMethodsInterface>()

    @Test
    fun testMockHasInstanceScope() {
        assertNotNull(MokkeryScope.from(mock))
    }
}
