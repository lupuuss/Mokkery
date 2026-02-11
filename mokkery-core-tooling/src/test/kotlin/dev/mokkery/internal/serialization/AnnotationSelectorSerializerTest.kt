package dev.mokkery.internal.serialization

import dev.mokkery.options.AnnotationSelector.Companion.all
import dev.mokkery.options.AnnotationSelector.Companion.matches
import dev.mokkery.options.AnnotationSelector.Companion.named
import dev.mokkery.options.AnnotationSelector.Companion.none
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class AnnotationSelectorSerializerTest {

    private val serializer = AnnotationSelectorSerializer

    @Test
    fun testSerializeAll() {
        assertEquals("all", serializer.serialize(all))
    }

    @Test
    fun testSerializeNone() {
        assertEquals("none", serializer.serialize(none))
    }


    @Test
    fun testSerializeEmptyNamed() {
        assertEquals("""named()""", serializer.serialize(named()))
    }

    @Test
    fun testSerializeOneElementNamed() {
        assertEquals("""named("example.a")""", serializer.serialize(named("example.a")))
    }

    @Test
    fun testSerializeMultipleElementsNamed() {
        assertEquals(
            """named("example.a"|"example.b"|"example.c")""",
            serializer.serialize(named("example.a", "example.b", "example.c")),
        )
    }

    @Test
    fun testSerializeEscapedString() {
        assertEquals(
            """named("\\ \n \t \r \b \' \"")""",
            serializer.serialize(named("\\ \n \t \r \b \' \""))
        )
    }

    @Test
    fun testSerializeMatchesNoOptions() {
        assertEquals("""matches("example.+")""", serializer.serialize(matches(Regex("example.+"))))
    }

    @Test
    fun testSerializeMatchesWithSingleOption() {
        assertEquals(
            """matches("example.+"|COMMENTS)""",
            serializer.serialize(matches(Regex("example.+", RegexOption.COMMENTS)))
        )
    }

    @Test
    fun testSerializeMatchesWithMultipleOption() {
        val optionsString = RegexOption.entries.joinToString(separator = "|")
        assertEquals(
            """matches("example.+"|${optionsString})""",
            serializer.serialize(matches(Regex(pattern = "example.+", options = RegexOption.entries.toSet())))
        )
    }

    @Test
    fun testSerializePlus() {
        assertEquals(
            """(matches("internal.+") + named("example.a"))""",
            serializer.serialize(matches("internal.+") + named("example.a"))
        )
    }

    @Test
    fun testSerializeMinus() {
        assertEquals(
            """(matches("internal.+") - named("example.a"))""",
            serializer.serialize(matches("internal.+") - named("example.a"))
        )
    }

    @Test
    fun testSerializeUnaryMinus() {
        assertEquals(
            """-named("example.a")""",
            serializer.serialize(-named("example.a"))
        )
    }

    @Test
    fun testSerializeMix() {
        assertEquals(
            """(all - (matches("external\\.internal.+") + named("example.internal.a")))""",
            serializer.serialize(all - (matches(Regex("external\\.internal.+")) + named("example.internal.a")))
        )
    }

    @Test
    fun testDeserializeAll() {
        assertEquals(all, serializer.deserialize("all"))
    }

    @Test
    fun testDeserializeNone() {
        assertEquals(none, serializer.deserialize("none"))
    }

    @Test
    fun testDeserializeEmptyNamed() {
        assertEquals(named(), serializer.deserialize("""named()"""))
    }

    @Test
    fun testDeserializeOneElementNamed() {
        assertEquals(
            named("example.a"),
            serializer.deserialize("""named("example.a")""")
        )
    }

    @Test
    fun testDeserializeMultipleElementsNamed() {
        assertEquals(
            named("example.a", "example.b", "example.c"),
            serializer.deserialize(
                """named("example.a"|"example.b"|"example.c")"""
            )
        )
        assertEquals(
            named("example.a", "example.b", "example.c"),
            serializer.deserialize(
                """named("example.a", "example.b", "example.c")"""
            )
        )
    }

    @Test
    fun testDeserializeEscapedString() {
        assertEquals(
            named("\\ \n \t \r \b \' \""),
            serializer.deserialize("""named("\\ \n \t \r \b \' \"")""")
        )
    }

    @Test
    fun testDeserializeMatchesNoOptions() {
        assertEquals(
            matches(Regex("example.+")),
            serializer.deserialize("""matches("example.+")""")
        )
    }

    @Test
    fun testDeserializeMatchesWithSingleOption() {
        assertEquals(
            matches(Regex("example.+", RegexOption.COMMENTS)),
            serializer.deserialize("""matches("example.+"|COMMENTS)""")
        )
    }

    @Test
    fun testDeserializeMatchesWithMultipleOptions() {
        val expectedRegex = Regex(
            pattern = "example.+",
            options = RegexOption.entries.toSet()
        )
        assertEquals(
            matches(expectedRegex),
            serializer.deserialize("""matches("example.+"|${RegexOption.entries.joinToString(separator = "|")})""")
        )
        assertEquals(
            matches(expectedRegex),
            serializer.deserialize("""matches("example.+", ${RegexOption.entries.joinToString(separator = ", ")})""")
        )
    }

    @Test
    fun testDeserializePlus() {
        assertEquals(
            matches("internal.+") + named("example.a"),
            serializer.deserialize("""matches("internal.+") + named("example.a")""")
        )
    }

    @Test
    fun testDeserializeMinus() {
        assertEquals(
            matches("internal.+") - named("example.a"),
            serializer.deserialize("""matches("internal.+") - named("example.a")""")
        )
    }

    @Test
    fun testDeserializeUnaryMinus() {
        assertEquals(
            -named("example.a"),
            serializer.deserialize("""-named("example.a")""")
        )
    }

    @Test
    fun testFailsWhileDeserializingEmptyString() {
        assertFails {
            serializer.deserialize("")
        }
    }

    @Test
    fun testDeserializeMix() {
        assertEquals(
            all - (matches(Regex("external\\.internal.+")) + named("example.internal.a")),
            serializer.deserialize("""(all - (matches("external\\.internal.+") + named("example.internal.a")))""")
        )
        assertEquals(
            all - (matches(Regex("external\\.internal.+")) + named("example.internal.a")),
            serializer.deserialize("""all - (matches("external\\.internal.+") + named("example.internal.a"))""")
        )
    }

}
