package dev.mokkery.test.debug

import dev.mokkery.MokkeryScope
import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.returnsArgAt
import dev.mokkery.debug.mokkeryDebugString
import dev.mokkery.every
import dev.mokkery.internal.mokkeryInternals
import dev.mokkery.internal.resetMocksCounter
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.test.RegularMethodsInterface
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(InternalMokkeryApi::class)
class MokkeryDebugStringTest {

    private fun MokkeryMatcherScope.stringStartsWith(value: String): String = matches(
        toString = { "stringStartsWith(\"$value\")" },
        predicate = { it.startsWith(value) }
    )

    @Test
    fun test() {
        MokkeryScope.global.mokkeryInternals.resetMocksCounter()
        val mock = mock<RegularMethodsInterface> {
            every { callUnit(Unit) } calls { }
            every { callPrimitive(any()) } returns 10
            every { callOverloaded(stringStartsWith("H")) } returnsArgAt 0
        }
        mock.callUnit(Unit)
        mock.callPrimitive(1)
        mock.callPrimitive(2)
        mock.callOverloaded("Hi!")
        assertEquals(
            expected = """
                mock {
                	id = dev.mokkery.test.RegularMethodsInterface(1)
                	mode = strict
                	answers {
                		callOverloaded(input = stringStartsWith("H")) returnsArgAt 0
                		callPrimitive(input = any()) returns 10
                		callUnit(unit = kotlin.Unit) calls {...}
                	}
                	calls {
                		callUnit(unit = kotlin.Unit)
                		callPrimitive(input = 1)
                		callPrimitive(input = 2)
                		callOverloaded(input = "Hi!")
                	}
                }
                
                """.trimIndent(),
            actual = mokkeryDebugString(mock)
        )
    }
}
