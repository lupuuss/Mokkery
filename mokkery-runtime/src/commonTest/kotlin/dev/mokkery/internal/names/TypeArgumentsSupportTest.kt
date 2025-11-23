package dev.mokkery.internal.names

import dev.mokkery.test.TestNameShortener
import kotlin.test.Test
import kotlin.test.assertEquals

class TypeArgumentsSupportTest {

    private val predefinedMappings = mapOf(
        "com.example.Foo" to "Foo",
        "dev.sample.Bar" to "Bar",
        "org.test.Far" to "Far",
        "kotlin.String" to "String",
        "kotlin.Map" to "Map",
    )
    private val baseShortener = TestNameShortener { it.associateWith(predefinedMappings::getValue) }
    private val nameShortener = baseShortener.withTypeArgumentsSupport()

    @Test
    fun testMapsRegularNamesWithBaseShortener() {
        assertEquals(predefinedMappings, nameShortener.shorten(predefinedMappings.keys))
    }

    @Test
    fun testMapsSingleRegularName() {
        assertEquals(mapOf("com.example.Foo" to "Foo"), nameShortener.shorten(setOf("com.example.Foo")))
    }

    @Test
    fun testMapsFlatArgumentsProperly() {
        val testName = "com.example.Foo<dev.sample.Bar, org.test.Far>"
        val expectedResult = mapOf(testName to "Foo<Bar, Far>")
        assertEquals(expectedResult, nameShortener.shorten(setOf(testName)))
    }

    @Test
    fun testMapsNestedTypeArgumentsProperly() {
        val testName = "com.example.Foo<dev.sample.Bar<kotlin.Map<kotlin.String, kotlin.String>>, org.test.Far>"
        val expectedResult = mapOf(testName to "Foo<Bar<Map<String, String>>, Far>")
        assertEquals(expectedResult, nameShortener.shorten(setOf(testName)))
    }

    @Test
    fun testMapsMultipleParameterizedNames() {
        val names = setOf(
            "com.example.Foo<dev.sample.Bar, org.test.Far>",
            "dev.sample.Bar<kotlin.Map<kotlin.String, kotlin.String>>",
            "kotlin.Map<kotlin.String, kotlin.String>"
        )
        val expected = mapOf(
            "com.example.Foo<dev.sample.Bar, org.test.Far>" to "Foo<Bar, Far>",
            "dev.sample.Bar<kotlin.Map<kotlin.String, kotlin.String>>" to "Bar<Map<String, String>>",
            "kotlin.Map<kotlin.String, kotlin.String>" to "Map<String, String>",
        )
        assertEquals(expected, nameShortener.shorten(names))
    }
}
