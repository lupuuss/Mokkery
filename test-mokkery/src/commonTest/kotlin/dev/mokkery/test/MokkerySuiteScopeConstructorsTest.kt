package dev.mokkery.test

import dev.mokkery.MokkerySuiteScope
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.mocks
import kotlin.test.Test
import kotlin.test.assertEquals

class SecondaryConstructorsSuite : MokkerySuiteScope {

    val mock: RegularMethodsInterface

    constructor(answer: Int) {
        mock = mock { every { callPrimitive(any()) } returns answer }
    }

    constructor() : this(0)
}

object ObjectSuite : MokkerySuiteScope {

    val mock = mock<RegularMethodsInterface> { every { callPrimitive(any()) } returns 3 }
}

class MokkerySuiteScopeConstructorsTest {

    @Test
    fun testSuiteWithoutPrimaryConstructorInitializesContext() {
        val suite = SecondaryConstructorsSuite(1)
        assertEquals(1, suite.mock.callPrimitive(0))
        assertEquals(listOf(suite.mock), suite.mocks)
    }

    @Test
    fun testSuiteWithDelegatingConstructorInitializesContextOnce() {
        val suite = SecondaryConstructorsSuite()
        assertEquals(0, suite.mock.callPrimitive(0))
        assertEquals(listOf(suite.mock), suite.mocks)
    }

    @Test
    fun testObjectSuiteInitializesContext() {
        assertEquals(3, ObjectSuite.mock.callPrimitive(0))
        assertEquals(listOf(ObjectSuite.mock), ObjectSuite.mocks)
    }
}
