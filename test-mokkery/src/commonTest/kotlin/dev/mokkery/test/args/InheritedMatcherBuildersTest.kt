package dev.mokkery.test.args

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.gt
import dev.mokkery.mock
import dev.mokkery.templating.ext
import dev.mokkery.test.ComplexArgsInterface
import dev.mokkery.test.GenericMatcherBuilders
import dev.mokkery.test.IntMatcherBuilders
import dev.mokkery.test.PlainMatcherBuilders
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InheritedMatcherBuildersTest {

    private val mock = mock<ComplexArgsInterface>()

    @Test
    fun testGenericExternalBuilderInheritedByLocalClass() {
        every { LocalIntMatcherBuilders().ext { mock.callPrimitive(inheritedOr(1, gt(5))) } } returns 1
        assertMatchesOneOrGreaterThanFive()
    }

    @Test
    fun testGenericExternalBuilderInheritedByExternalClass() {
        every { IntMatcherBuilders().ext { mock.callPrimitive(inheritedOr(1, gt(5))) } } returns 1
        assertMatchesOneOrGreaterThanFive()
    }

    @Test
    fun testExternalBuilderInheritedByLocalClass() {
        every { LocalPlainMatcherBuilders().ext { mock.callPrimitive(inheritedOr(1, gt(5))) } } returns 1
        assertMatchesOneOrGreaterThanFive()
    }

    private fun assertMatchesOneOrGreaterThanFive() {
        assertEquals(1, mock.callPrimitive(1))
        assertEquals(1, mock.callPrimitive(6))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(3) }
    }
}

private class LocalIntMatcherBuilders : GenericMatcherBuilders<Int>()

private class LocalPlainMatcherBuilders : PlainMatcherBuilders()
