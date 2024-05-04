package dev.mokkery.internal.names

import dev.mokkery.test.TestMockUniqueReceiversGenerator
import dev.mokkery.test.TestNameShortener
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.collections.associateWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.text.removePrefix
import kotlin.text.substringBefore

class GroupMockReceiverShortenerTest {

    private val namesShortener = TestNameShortener { names ->
        names.associateWith { it.removePrefix("package.") }
    }
    private val generator = TestMockUniqueReceiversGenerator(
        extractTypeCalls = { it.substringBefore("@") }
    )
    private val shortener = GroupMockReceiverShortener(namesShortener = namesShortener, receiversGenerator = generator)

    private val templates = listOf(
        fakeCallTemplate(receiver = "package.foo@1"),
        fakeCallTemplate(receiver = "package.foo@2"),
        fakeCallTemplate(receiver = "package.test@4"),
        fakeCallTemplate(receiver = "package.a@5"),
    )
    private val traces = listOf(
        fakeCallTrace(receiver = "package.foo@1", orderStamp = 1),
        fakeCallTrace(receiver = "package.bar@2", orderStamp = 2),
        fakeCallTrace(receiver = "package.bar@2", orderStamp = 3),
        fakeCallTrace(receiver = "package.far@3", orderStamp = 4),
    )

    @Test
    fun testMapsTemplatesWithShorterNames() {
        val expectedTemplates = listOf(
            fakeCallTemplate(receiver = "foo@1"),
            fakeCallTemplate(receiver = "foo@2"),
            fakeCallTemplate(receiver = "test@4"),
            fakeCallTemplate(receiver = "a@5"),
        )
        shortener.prepare(traces, templates)
        assertEquals(expectedTemplates, shortener.shortenTemplates(templates))
    }

    @Test
    fun testMapsTracesWithShorterNames() {
        val expectedTraces = listOf(
            fakeCallTrace(receiver = "foo@1", orderStamp = 1),
            fakeCallTrace(receiver = "bar@2", orderStamp = 2),
            fakeCallTrace(receiver = "bar@2", orderStamp = 3),
            fakeCallTrace(receiver = "far@3", orderStamp = 4),
        )
        shortener.prepare(traces, templates)
        assertEquals(expectedTraces, shortener.shortenTraces(traces))
    }

    @Test
    fun testReturnsOriginalCallForCallWithShorterName() {
        shortener.prepare(traces, templates)
        shortener.shortenTraces(traces)
        assertEquals(
            expected = fakeCallTrace(receiver = "package.bar@2", orderStamp = 3),
            actual = shortener.getOriginalTrace(fakeCallTrace(receiver = "bar@2", orderStamp = 3))
        )
    }
}
