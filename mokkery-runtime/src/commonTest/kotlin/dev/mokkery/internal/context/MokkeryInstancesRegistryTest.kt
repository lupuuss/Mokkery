package dev.mokkery.internal.context

import dev.mokkery.internal.instanceId
import dev.mokkery.test.TestMokkeryInstanceScope
import kotlin.test.Test
import kotlin.test.assertEquals

class MokkeryInstancesRegistryTest {

    private val instance1 = TestMokkeryInstanceScope(sequence = 1)
    private val instance2 = TestMokkeryInstanceScope(sequence = 2)

    @Test
    fun testCollectionContainsInitialAndRegisteredInstances() {
        val registry = MokkeryInstancesRegistry(listOf(instance1))
        registry.register(instance2)
        assertEquals(setOf(instance1.instanceId, instance2.instanceId), registry.collection.ids)
    }

    @Test
    fun testCollectionIsNotAffectedByRegistrationsMadeAfterItWasReturned() {
        val registry = MokkeryInstancesRegistry(listOf(instance1))
        val collection = registry.collection
        registry.register(instance2)
        assertEquals(setOf(instance1.instanceId), collection.ids)
    }

    @Test
    fun testCollectionIsNotAffectedByRegistrationsWhenInitiallyEmpty() {
        val registry = MokkeryInstancesRegistry()
        val collection = registry.collection
        registry.register(instance1)
        assertEquals(emptySet(), collection.ids)
    }
}
