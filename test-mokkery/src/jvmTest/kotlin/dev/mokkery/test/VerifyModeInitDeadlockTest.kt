package dev.mokkery.test

import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verify.VerifyModeInternals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

class VerifyModeInitDeadlockTest {

    @OptIn(InternalMokkeryApi::class)
    @Test
    fun test() {
        runBlocking {
           repeat(10_000) {
               val interrupted = withTimeoutOrNull(1.seconds) {
                   runInterruptible {
                       val gate = CountDownLatch(1)
                       val a = thread(start = false) {
                           gate.await()
                           VerifyMode.soft
                       }
                       val b = thread(start = false) {
                           gate.await()
                           VerifyModeInternals.Soft(1, 1)
                       }
                       a.start()
                       b.start()
                       gate.countDown()
                       a.join()
                       b.join()   // may never return
                   }
               }
               assertNotNull(actual = interrupted, message = "Deadlock detected!")
           }
        }
    }
}
