package dev.mokkery.internal

import dev.mokkery.test.TestMokkeryInstanceScope
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MokkeryCollectionTest {

    private val instance1 = TestMokkeryInstanceScope(sequence = 0)
    private val instance2 = TestMokkeryInstanceScope(sequence = 1)
    private val instance3 = TestMokkeryInstanceScope(sequence = 2)

    @Test
    fun testEmptyCollection() {
        val collection = MokkeryCollection()
        assertEquals(emptySet(), collection.ids)
        assertContentEquals(emptyList(), collection.scopes)
        assertEquals(null, collection.getScopeOrNull(instance1.instanceId))
        assertEquals(null, collection.getScopeOrNull(instance2.instanceId))
        assertEquals(null, collection.getScopeOrNull(instance3.instanceId))
        assertEquals(MokkeryCollection(), collection)
        assertNotEquals(MokkeryCollection(instance1), collection)
        assertNotEquals(MokkeryCollection(instance1, instance2), collection)
    }

    @Test
    fun testSingletonCollection() {
        val collection = MokkeryCollection(instance1)
        assertEquals(setOf(instance1.instanceId), collection.ids)
        assertContentEquals(listOf(instance1), collection.scopes)
        assertEquals(instance1, collection.getScopeOrNull(instance1.instanceId))
        assertEquals(null, collection.getScopeOrNull(instance2.instanceId))
        assertEquals(null, collection.getScopeOrNull(instance3.instanceId))
        assertEquals(MokkeryCollection(instance1), collection)
        assertNotEquals(MokkeryCollection(instance2), collection)
        assertNotEquals(MokkeryCollection(instance1, instance2), collection)
    }

    @Test
    fun testMultipleInstancesCollection() {
        val collection = MokkeryCollection(instance1, instance2)
        assertEquals(setOf(instance1.instanceId, instance2.instanceId), collection.ids)
        assertContentEquals(listOf(instance1, instance2), collection.scopes)
        assertEquals(instance1, collection.getScopeOrNull(instance1.instanceId))
        assertEquals(instance2, collection.getScopeOrNull(instance2.instanceId))
        assertEquals(null, collection.getScopeOrNull(instance3.instanceId))
        assertEquals(MokkeryCollection(instance1, instance2), collection)
        assertNotEquals(MokkeryCollection(), collection)
        assertNotEquals(MokkeryCollection(instance1), collection)
        assertNotEquals(MokkeryCollection(instance2), collection)
        assertNotEquals(MokkeryCollection(instance2, instance3), collection)
    }

    @Test
    fun testPlus() {
        assertEquals(
            expected = MokkeryCollection(instance1, instance2, instance3),
            actual = MokkeryCollection(instance1, instance2) + MokkeryCollection(instance3)
        )
        assertEquals(
            expected = MokkeryCollection(instance1, instance3),
            actual = MokkeryCollection(instance1) + MokkeryCollection(instance3)
        )
        assertEquals(
            expected = MokkeryCollection(instance1, instance2),
            actual = MokkeryCollection(instance1, instance2) + MokkeryCollection()
        )
        assertEquals(
            expected = MokkeryCollection(instance1, instance2, instance3),
            actual = MokkeryCollection(instance1, instance2, instance3) + MokkeryCollection(instance1)
        )
        assertEquals(expected = MokkeryCollection(), actual = MokkeryCollection() + MokkeryCollection())
    }
}
