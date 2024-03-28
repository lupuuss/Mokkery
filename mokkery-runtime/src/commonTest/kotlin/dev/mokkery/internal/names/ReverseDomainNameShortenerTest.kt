package dev.mokkery.internal.names

import kotlin.test.Test
import kotlin.test.assertEquals

class ReverseDomainNameShortenerTest {

    private val shortener = ReverseDomainNameShortener

    @Test
    fun testLeavesOnlyLastSequenceOfNameIfUnique() {
        val names = setOf(
            "com.example.Foo",
            "dev.sample.Bar",
            "org.test.Far",
        )
        val expectedShorterNames = mapOf(
            "com.example.Foo" to "Foo",
            "dev.sample.Bar" to "Bar",
            "org.test.Far" to "Far",
        )
        assertEquals(expectedShorterNames, shortener.shorten(names))
    }

    @Test
    fun testLeavesOnlyLastSequenceOfNameIfUniqueWhenDifferentNumberOfSequences() {
        val names = setOf(
            "Foo",
            "dev.Bar",
            "org.test.Far",
        )
        val expectedShorterNames = mapOf(
            "Foo" to "Foo",
            "dev.Bar" to "Bar",
            "org.test.Far" to "Far",
        )
        assertEquals(expectedShorterNames, shortener.shorten(names))
    }

    @Test
    fun testKeepsContextUntilDifferentForNamesWithSimilarParts() {
        val names = setOf(
            "org.test.foo.Component",
            "org.test.bar.Component",
            "org.test.far.Component",
        )
        val expectedShorterNames = mapOf(
            "org.test.foo.Component" to "foo.Component",
            "org.test.bar.Component" to "bar.Component",
            "org.test.far.Component" to "far.Component",
        )
        assertEquals(expectedShorterNames, shortener.shorten(names))
    }

    @Test
    fun testKeepsContextOnlyForNamesWithSimilarParts() {
        val names = setOf(
            "org.test.test.Test",
            "org.test.foo.Component",
            "org.test.bar.Component",
            "org.test.far.Component",
        )
        val expectedShorterNames = mapOf(
            "org.test.test.Test" to "Test",
            "org.test.foo.Component" to "foo.Component",
            "org.test.bar.Component" to "bar.Component",
            "org.test.far.Component" to "far.Component",
        )
        assertEquals(expectedShorterNames, shortener.shorten(names))
    }

    @Test
    fun testRemovesFirstSequenceIfSecondsMakesItUnique() {
        val names = setOf(
            "org.foo.Component",
            "org.bar.Component",
            "org.far.Component",
        )
        val expectedShorterNames = mapOf(
            "org.foo.Component" to "foo.Component",
            "org.bar.Component" to "bar.Component",
            "org.far.Component" to "far.Component",
        )
        assertEquals(expectedShorterNames, shortener.shorten(names))
    }

    @Test
    fun testKeepsAllPartsIfOnlyFirstPartMakesItUnique() {
        val names = setOf(
            "org.test.Component",
            "com.test.Component",
            "dev.test.Component",
        )
        val expectedShorterNames = mapOf(
            "org.test.Component" to "org.test.Component",
            "com.test.Component" to "com.test.Component",
            "dev.test.Component" to "dev.test.Component",
        )
        assertEquals(expectedShorterNames, shortener.shorten(names))
    }
}
