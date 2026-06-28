package dev.mokkery.test

import dev.mokkery.MokkeryScope
import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertNotNull

@OptIn(InternalMokkeryApi::class)
class MokkeryInstanceScopeTest {

    private val mock = mock<RegularMethodsInterface>()

    @Test
    fun testMockHasInstanceScope() {
        assertNotNull(MokkeryScope.extract(mock))
    }
}
