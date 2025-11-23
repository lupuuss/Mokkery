package dev.mokkery.internal.names

import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.instanceId
import dev.mokkery.test.TestMokkeryInstanceScope
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertEquals

class AliasMokkeryInstanceCollectionTest {

    private val instances = MokkeryCollection(
        TestMokkeryInstanceScope(typeName = "package.foo", sequence = 0),
        TestMokkeryInstanceScope(typeName = "package.test", sequence = 1),
        TestMokkeryInstanceScope(typeName = "package.a", sequence = 2),
        TestMokkeryInstanceScope(typeName = "package.bar", sequence = 3),
        TestMokkeryInstanceScope(typeName = "package.far", sequence = 4),
    )
    private val aliasMocks = instances.withAliasing { it.copy(typeName = it.typeName.removePrefix("package.")) }

    private val templates = listOf(
        fakeCallTemplate(typeName = "package.foo", id = 0),
        fakeCallTemplate(typeName = "package.foo", id = 0),
        fakeCallTemplate(typeName = "package.test", id = 1),
        fakeCallTemplate(typeName = "package.a", id = 2),
    )
    private val traces = listOf(
        fakeCallTrace(typeName = "package.foo", id = 0, orderStamp = 1),
        fakeCallTrace(typeName = "package.bar", id = 3, orderStamp = 2),
        fakeCallTrace(typeName = "package.bar", id = 3, orderStamp = 3),
        fakeCallTrace(typeName = "package.far", id = 4, orderStamp = 4),
    )

    @Test
    fun testAliasTemplatesWithShorterNames() {
        val expectedTemplates = listOf(
            fakeCallTemplate(typeName = "foo", id = 0),
            fakeCallTemplate(typeName = "foo", id = 0),
            fakeCallTemplate(typeName = "test", id = 1),
            fakeCallTemplate(typeName = "a", id = 2),
        )
        assertEquals(expectedTemplates, aliasMocks.aliasTemplates(templates))
    }

    @Test
    fun testAliasTracesWithShorterNames() {
        val expectedTraces = listOf(
            fakeCallTrace(typeName = "foo", id = 0, orderStamp = 1),
            fakeCallTrace(typeName = "bar", id = 3, orderStamp = 2),
            fakeCallTrace(typeName = "bar", id = 3, orderStamp = 3),
            fakeCallTrace(typeName = "far", id = 4, orderStamp = 4),
        )
        assertEquals(expectedTraces, aliasMocks.aliasTraces(traces))
    }

    @Test
    fun testMapsOriginalToAlias() {
        assertEquals(MokkeryInstanceId("foo", 0), aliasMocks.mapOriginalToAlias(instances.scopes.first().instanceId))
    }

    @Test
    fun testMapsAliasToOriginal() {
        assertEquals(MokkeryInstanceId("package.foo", 0), aliasMocks.mapAliasToOriginal(MokkeryInstanceId("foo", 0)))
    }
}
