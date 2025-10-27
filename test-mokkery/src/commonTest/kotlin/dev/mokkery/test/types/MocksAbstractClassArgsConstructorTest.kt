package dev.mokkery.test.types

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.test.AbstractClassArgsConstructor
import dev.mokkery.test.ignoreOnWasm
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class MocksAbstractClassArgsConstructorTest {

    @Test
    fun testCall() = ignoreOnWasm("It seems that since Kotlin 2.2.20 null is no longer acceptable for constructor argument") {
        val mock = mock<AbstractClassArgsConstructor>()
        every { mock.call(any()) } returns 10
        assertEquals(10, mock.call(1))
        verify { mock.call(any()) }
    }
}
