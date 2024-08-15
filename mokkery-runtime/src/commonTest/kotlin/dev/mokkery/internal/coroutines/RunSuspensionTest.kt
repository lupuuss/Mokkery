package dev.mokkery.internal.coroutines

import dev.mokkery.answering.Answer
import dev.mokkery.internal.IllegalSuspensionException
import dev.mokkery.test.fakeFunctionScope
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
    fun testRunsWholeBlockInPlaceWithSuspendFunctionsWithoutSuspensionPoints() {
        val checkpoints = mutableListOf<Int>()
        runSuspension {
            checkpoints.add(1)
            noActualSuspensionFun()
            checkpoints.add(2)
            noActualSuspensionFun()
            checkpoints.add(3)
        }
        assertEquals(listOf(1, 2, 3), checkpoints)
    }

    @Test
    fun testThrowsNestedException() {
        assertFailsWith<IllegalArgumentException> {
            runSuspension {
                noActualSuspensionFun()
                throw IllegalArgumentException()
            }
        }
    }

    @Test
    fun testFailsWithActualSuspension() {
        assertFailsWith<IllegalSuspensionException> {
            runSuspension { suspendCoroutine {  } }
        }
    }

    private suspend fun noActualSuspensionFun(): Int = Answer.Const(1).callSuspend(fakeFunctionScope())
}
