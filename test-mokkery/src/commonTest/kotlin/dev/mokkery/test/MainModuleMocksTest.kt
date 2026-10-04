package dev.mokkery.test

import kotlin.test.Test
import kotlin.test.assertEquals

class MainModuleMocksTest {

    @Test
    fun testMockCreatedInMainModuleIsUsableFromTestModule() {
        assertEquals(5, mockCreatedInMainModule(5).callPrimitive(1))
    }
}
