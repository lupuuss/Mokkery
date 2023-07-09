package dev.mokkery.test

import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import kotlin.test.Test
import kotlin.test.assertFailsWith

class VerifyTest {

    private val dependencyMock = mock<TestInterface>()

    @Test
    fun testVerifiesRegularMethodCallWithPrimitiveTypes() {
        assertVerified {
            verify {
                dependencyMock.callWithPrimitives(1)
            }
        }
    }

    @Test
    fun testVerifiesRegularMethodCallWithComplexTypes() {
        assertVerified {
            verify {
                dependencyMock.callWithComplex(any())
            }
        }
    }

    @Test
    fun testVerifiesRegularMethodWithExtensionReceiver() {
        assertVerified {
            verify {
                dependencyMock.run { 1.callWithExtensionReceiver() }
            }
        }
    }

    @Test
    fun testVerifiesMethodsWithAnyVarargs() {
        assertVerified {
            verify {
                dependencyMock.callWithVararg(1, "2")
            }
        }
    }

    @Test
    fun testVerifiesSuspendingMethods() {
        assertVerified {
            verifySuspend {
                dependencyMock.callWithSuspension(1)
            }
        }
    }

    @Test
    fun testMocksMethodsWithNothingReturnType() {
        assertVerified {
            verify {
                dependencyMock.callNothing()
            }
        }
    }

    private inline fun assertVerified(crossinline block: () -> Unit) {
        assertFailsWith<AssertionError> {
            block()
        }
    }
}
