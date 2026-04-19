package dev.mokkery.test.types

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.test.CustomMockableClass
import dev.mokkery.test.CustomMockableDependingOnCustomClass
import dev.mokkery.test.CustomMockableDependingOnStandardClass
import dev.mokkery.test.MockableClass
import dev.mokkery.test.MockableDependingClass
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class MocksMockableClassTest {

    @Test
    fun testWithStandardAnnotation() {
        val m = mock<MockableClass> {
            every { call() } returns 2
        }
        assertEquals(2, m.call())
        verify { m.call() }
    }

    @Test
    fun testWithStandardAnnotationDependingOnStandardMockable() {
        val m = mock<MockableDependingClass> {
            every { baseCall() } returns 2
        }
        assertEquals(2, m.baseCall())
        verify { m.baseCall() }
    }

    @Test
    fun testWithCustomAnnotation() {
        val m = mock<CustomMockableClass> {
            every { call() } returns 2
        }
        assertEquals(2, m.call())
        verify { m.call() }
    }

    @Test
    fun testWithCustomAnnotationDependingOnCustomMockable() {
        val m = mock<CustomMockableDependingOnCustomClass> {
            every { baseCall() } returns 2
        }
        assertEquals(2, m.baseCall())
        verify { m.baseCall() }
    }

    @Test
    fun testWithCustomAnnotationDependingOnStandardMockable() {
        val m = mock<CustomMockableDependingOnStandardClass> {
            every { baseCall() } returns 2
        }
        assertEquals(2, m.baseCall())
        verify { m.baseCall() }
    }
}
