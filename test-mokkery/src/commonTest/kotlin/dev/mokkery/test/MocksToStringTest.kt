package dev.mokkery.test

import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertTrue

class MocksToStringTest {

    @Test
    fun testRegularMocksToString() {
        assertTrue {
            mock<RegularMethodsInterface>()
                .toString()
                .matches(Regex("dev.mokkery.test.RegularMethodsInterface\\(\\d+\\)"))
        }
    }

    @Test
    fun testFunctionMocksToString() {
        assertTrue {
            mock<(Int) -> String>()
                .toString()
                .matches(Regex("kotlin.Function1\\(\\d+\\)"))
        }
    }
}
