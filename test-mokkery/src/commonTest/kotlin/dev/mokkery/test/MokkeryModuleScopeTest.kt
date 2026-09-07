package dev.mokkery.test

import dev.mokkery.MockMode
import dev.mokkery.MokkeryScope
import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.internal.defaultMockMode
import dev.mokkery.internal.defaultVerifyMode
import dev.mokkery.internal.moduleName
import dev.mokkery.internal.mokkeryInternals
import dev.mokkery.module
import dev.mokkery.verify.VerifyMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(InternalMokkeryApi::class)
class MokkeryModuleScopeTest {

    @Test
    fun test() {
        val module = assertNotNull(MokkeryScope.module)
        val internals = module.mokkeryInternals
        assertEquals("dev.mokkery:test-mokkery_test", internals.moduleName)
        assertEquals(MockMode.strict, internals.defaultMockMode)
        assertEquals(VerifyMode.soft, internals.defaultVerifyMode)
        assertEquals("MokkeryScope(mokkeryContext=${module.mokkeryContext})", module.toString())
    }
}
