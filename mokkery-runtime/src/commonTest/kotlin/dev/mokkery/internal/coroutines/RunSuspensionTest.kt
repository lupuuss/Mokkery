package dev.mokkery.internal.coroutines

import kotlinx.coroutines.delay
import kotlin.test.Test
import kotlin.test.assertEquals

class RunSuspensionTest {

    @Test
    fun testRunsWholeBlockInPlaceWithoutSuspension() {
        val checkpoints = mutableListOf<Int>()
        runSuspension {
            checkpoints.add(1)
            checkpoints.add(2)
            checkpoints.add(3)
        }
        assertEquals(listOf(1, 2, 3), checkpoints)
    }

    @Test
    fun testRunsWholeBlockInPlaceWithSuspension() {
        val checkpoints = mutableListOf<Int>()
        runSuspension {
            checkpoints.add(1)
            delay(2)
            checkpoints.add(2)
            delay(2)
            checkpoints.add(3)
        }
        assertEquals(listOf(1, 2, 3), checkpoints)
    }
}
