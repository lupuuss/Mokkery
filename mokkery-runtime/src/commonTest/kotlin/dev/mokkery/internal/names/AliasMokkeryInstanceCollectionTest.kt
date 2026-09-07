package dev.mokkery.internal.names

import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.instanceId
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.test.TestMokkeryInstanceScope
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertEquals

class AliasMokkeryInstanceCollectionTest {

    private val instances = MokkeryCollection(
        TestMokkeryInstanceScope(instanceId = 0, typeName = "package.foo"),
        TestMokkeryInstanceScope(instanceId = 1, typeName = "package.test"),
        TestMokkeryInstanceScope(instanceId = 2, typeName = "package.a"),
        TestMokkeryInstanceScope(instanceId = 3, typeName = "package.bar"),
        TestMokkeryInstanceScope(instanceId = 4, typeName = "package.far"),
    )
    private val aliasMocks = instances.withAliasing { it.copy(typeName = it.typeName.removePrefix("package.")) }

    private val templates = listOf(
        fakeCallTemplate(typeName = "package.foo", instanceId = 0),
        fakeCallTemplate(typeName = "package.foo", instanceId = 0),
        fakeCallTemplate(typeName = "package.test", instanceId = 1),
        fakeCallTemplate(typeName = "package.a", instanceId = 2),
    )
    private val traces = listOf(
        fakeCallTrace(traceId = 1, instanceId = 0, typeName = "package.foo"),
        fakeCallTrace(traceId = 2, instanceId = 3, typeName = "package.bar"),
        fakeCallTrace(traceId = 3, instanceId = 3, typeName = "package.bar"),
        fakeCallTrace(traceId = 4, instanceId = 4, typeName = "package.far"),
    )

    @Test
    fun testAliasTemplatesWithShorterNames() {
        val expectedTemplates = listOf(
            fakeCallTemplate(typeName = "foo", instanceId = 0),
            fakeCallTemplate(typeName = "foo", instanceId = 0),
            fakeCallTemplate(typeName = "test", instanceId = 1),
            fakeCallTemplate(typeName = "a", instanceId = 2),
        )
        assertEquals(expectedTemplates, aliasMocks.aliasTemplates(templates))
    }

    @Test
    fun testAliasTracesWithShorterNames() {
        val expectedInstanceIds = listOf(
            MokkeryInstanceId("foo", 0),
            MokkeryInstanceId("bar", 3),
            MokkeryInstanceId("bar", 3),
            MokkeryInstanceId("far", 4),
        )
        assertEquals(expectedInstanceIds, aliasMocks.aliasTraces(traces).map(CallTrace::instanceId))
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

private fun AliasMokkeryCollection.aliasTraces(
    traces: List<CallTrace>
): List<CallTrace> = traces.map { aliasTrace(it) }

private fun AliasMokkeryCollection.aliasTemplates(
    templates: List<CallTemplate>
): List<CallTemplate> = templates.map { aliasTemplate(it) }

private fun AliasMokkeryCollection.aliasTrace(
    trace: CallTrace
) = CallTrace(
    instanceId = mapOriginalToAlias(trace.instanceId),
    functionId = trace.functionId,
    args = trace.args,
    id = trace.id,
)

private fun AliasMokkeryCollection.aliasTemplate(
    template: CallTemplate
) = template.copy(instanceId = mapOriginalToAlias(template.instanceId))
