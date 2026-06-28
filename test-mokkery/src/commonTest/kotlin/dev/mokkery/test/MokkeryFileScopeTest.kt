package dev.mokkery.test

import dev.mokkery.MockMode
import dev.mokkery.MokkeryScope
import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.internal.defaultMockMode
import dev.mokkery.internal.defaultVerifyMode
import dev.mokkery.internal.mokkeryInternals
import dev.mokkery.verify.VerifyMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(InternalMokkeryApi::class)
class MokkeryFileScopeTest {

    @Test
    fun test() {
        val file = assertNotNull(MokkeryScope.file)
        assertEquals(MockMode.strict, file.mokkeryInternals.defaultMockMode)
        assertEquals(VerifyMode.soft, file.mokkeryInternals.defaultVerifyMode)
        assertEquals(
            "MokkeryScope(mokkeryContext=${file.mokkeryContext})",
            file.toString()
        )
    }
}
