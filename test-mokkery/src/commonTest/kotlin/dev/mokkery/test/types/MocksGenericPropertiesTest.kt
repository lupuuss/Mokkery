package dev.mokkery.test.types

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.test.GenericPropertiesInterface
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class MocksGenericPropertiesTest {

    private val mock = mock<GenericPropertiesInterface<Int?>>()

    @Test
    fun testProperty() {
        every { mock.property } returns 1
        every { mock.property = any<Int>() } returns Unit
        assertEquals(1, mock.property)
        mock.property = 2
        verify {
            mock.property
            mock.property = 2
        }
    }

    @Test
    fun testExtProperty() {
        every { mock.run { any<String>().extProperty } } returns 1
        every { mock.run { any<String>().extProperty = any<Int>() } } returns Unit
        assertEquals(1, mock.run { "1".extProperty })
        mock.run { "2".extProperty = 2 }
        verify {
            mock.run {
                "1".extProperty
                "2".extProperty = 2
            }
        }
    }

    @Test
    fun testExtPropertyNullabilityMarkers() {
        every { mock.run { any<String>().extPropertyNullabilityMarkers } } returns 1
        every { mock.run { any<String>().extPropertyNullabilityMarkers = any<Int>() } } returns Unit
        assertEquals(1, mock.run { "1".extPropertyNullabilityMarkers })
        mock.run { "2".extPropertyNullabilityMarkers = 2 }
        verify {
            mock.run {
                "1".extPropertyNullabilityMarkers
                "2".extPropertyNullabilityMarkers = 2
            }
        }
    }

    @Test
    fun testExtPropertyNestedNullabilityMarkers() {
        every { mock.run { any<List<String>>().extPropertyNestedNullabilityMarkers } } returns listOf(1)
        every { mock.run { any<List<String>>().extPropertyNestedNullabilityMarkers = any<List<Int>>() } } returns Unit
        assertEquals(listOf(1), mock.run { listOf("1").extPropertyNestedNullabilityMarkers })
        mock.run { listOf("2").extPropertyNestedNullabilityMarkers = listOf(2) }
        verify {
            mock.run {
                listOf("1").extPropertyNestedNullabilityMarkers
                listOf("2").extPropertyNestedNullabilityMarkers = listOf(2)
            }
        }
    }


    @Test
    fun testExtPropertyBound() {
        every { mock.run { any<String>().extPropertyBound } } returns 1
        every { mock.run { any<String>().extPropertyBound = any<Int>() } } returns Unit
        assertEquals(1, mock.run { "1".extPropertyBound })
        mock.run { "2".extPropertyBound = 2 }
        verify {
            mock.run {
                "1".extPropertyBound
                "2".extPropertyBound = 2
            }
        }
    }

    @Test
    fun testExtPropertyBoundRecursiveParam() {
        every { mock.run { any<String>().extPropertyBoundRecursiveParam } } returns 1
        every { mock.run { any<String>().extPropertyBoundRecursiveParam = any<Int>() } } returns Unit
        assertEquals(1, mock.run { "1".extPropertyBoundRecursiveParam })
        mock.run { "2".extPropertyBoundRecursiveParam = 2 }
        verify {
            mock.run {
                "1".extPropertyBoundRecursiveParam
                "2".extPropertyBoundRecursiveParam = 2
            }
        }
    }

    @Test
    fun testExtPropertyBoundParentParam() {
        every { mock.run { any<Int>().extPropertyBoundParentParam } } returns 1
        every { mock.run { any<Int>().extPropertyBoundParentParam = any<Int>() } } returns Unit
        assertEquals(1, mock.run { 1.extPropertyBoundParentParam })
        mock.run { 2.extPropertyBoundParentParam = 2 }
        verify {
            mock.run {
                1.extPropertyBoundParentParam
                2.extPropertyBoundParentParam = 2
            }
        }
    }

    @Test
    fun testExtPropertyBoundNestedParentParam() {
        every { mock.run { any<List<Int>>().extPropertyBoundNestedParentParam } } returns 1
        every { mock.run { any<List<Int>>().extPropertyBoundNestedParentParam = any<Int>() } } returns Unit
        assertEquals(1, mock.run { listOf(1).extPropertyBoundNestedParentParam })
        mock.run { listOf(2).extPropertyBoundNestedParentParam = 2 }
        verify {
            mock.run {
                listOf(1).extPropertyBoundNestedParentParam
                listOf(2).extPropertyBoundNestedParentParam = 2
            }
        }
    }

    @Test
    fun testExtPropertyMultipleBounds() {
        every { mock.run { any<String>().extPropertyMultipleBounds } } returns 1
        every { mock.run { any<String>().extPropertyMultipleBounds = any<Int>() } } returns Unit
        assertEquals(1, mock.run { "1".extPropertyMultipleBounds })
        mock.run { "2".extPropertyMultipleBounds = 2 }
        verify {
            mock.run {
                "1".extPropertyMultipleBounds
                "2".extPropertyMultipleBounds = 2
            }
        }
    }

    @Test
    fun testExtPropertyStarProjection() {
        every { mock.run { any<List<Int>>().extPropertyStarProjection } } returns listOf(1)
        every { mock.run { any<List<Int>>().extPropertyStarProjection = any<List<Int>>() } } returns Unit
        assertEquals(listOf(1), mock.run { listOf(1).extPropertyStarProjection })
        mock.run { listOf(2).extPropertyStarProjection =listOf(2) }
        verify {
            mock.run {
                listOf(1).extPropertyStarProjection
                listOf(2).extPropertyStarProjection = listOf(2)
            }
        }
    }

    @Test
    fun testExtPropertySelf() {
        val subMock = mock<SubGenericPropertiesInterface>()
        every { mock.run { any<SubGenericPropertiesInterface>().extPropertySelf } } returns mock
        every { mock.run { any<SubGenericPropertiesInterface>().extPropertySelf = any<GenericPropertiesInterface<Int?>>() } } returns Unit
        assertEquals(mock, mock.run { subMock.extPropertySelf })
        mock.run { subMock.extPropertySelf = mock }
        verify {
            mock.run {
                subMock.extPropertySelf
                subMock.extPropertySelf = mock
            }
        }
    }
}
private interface SubGenericPropertiesInterface : GenericPropertiesInterface<SubGenericPropertiesInterface>
