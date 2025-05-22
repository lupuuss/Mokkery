package dev.mokkery.internal.names

import dev.mokkery.test.TestNameShortener
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertEquals

class GroupMockReceiverShortenerTest {

    private val namesShortener = TestNameShortener { names ->
        names.associateWith { it.removePrefix("package.") }
    }
    private val shortener = GroupMockReceiverShortener(namesShortener = namesShortener)

    private val templates = listOf(
        fakeCallTemplate(typeName = "package.foo", id = 1),
        fakeCallTemplate(typeName = "package.foo", id = 2),
        fakeCallTemplate(typeName = "package.test", id = 4),
        fakeCallTemplate(typeName = "package.a", id = 5),
    )
    private val traces = listOf(
        fakeCallTrace(typeName = "package.foo", id = 1, orderStamp = 1),
        fakeCallTrace(typeName = "package.bar", id = 2, orderStamp = 2),
        fakeCallTrace(typeName = "package.bar", id = 2, orderStamp = 3),
        fakeCallTrace(typeName = "package.far", id = 3, orderStamp = 4),
    )

    @Test
    fun testMapsTemplatesWithShorterNames() {
        val expectedTemplates = listOf(
            fakeCallTemplate(typeName = "foo", id = 1),
            fakeCallTemplate(typeName = "foo", id = 2),
            fakeCallTemplate(typeName = "test", id = 4),
            fakeCallTemplate(typeName = "a", id = 5),
        )
        shortener.prepare(traces, templates)
        assertEquals(expectedTemplates, shortener.shortenTemplates(templates))
    }

    @Test
    fun testMapsTracesWithShorterNames() {
        val expectedTraces = listOf(
            fakeCallTrace(typeName = "foo", id = 1, orderStamp = 1),
            fakeCallTrace(typeName = "bar", id = 2, orderStamp = 2),
            fakeCallTrace(typeName = "bar", id = 2, orderStamp = 3),
            fakeCallTrace(typeName = "far", id = 3, orderStamp = 4),
        )
        shortener.prepare(traces, templates)
        assertEquals(expectedTraces, shortener.shortenTraces(traces))
    }

    @Test
    fun testReturnsOriginalCallForCallWithShorterName() {
        shortener.prepare(traces, templates)
        shortener.shortenTraces(traces)
        assertEquals(
            expected = fakeCallTrace(typeName = "package.bar", id = 2, orderStamp = 3),
            actual = shortener.getOriginalTrace(fakeCallTrace(typeName = "bar", id = 2, orderStamp = 3))
        )
    }
}
