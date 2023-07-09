package dev.mokkery.test

import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import kotlin.test.Test

class VerifyGenericTest {

    private val dependencyMock = mock<TestGenericInterface<String>>()

    @Test
    fun testVerifiesRegularPropertiesCalls() {
        assertVerified {
            verify {
                dependencyMock.value
            }
        }
    }

    @Test
    fun testVerifiesRegularMethodCalls() {
        assertVerified {
            verify {
                dependencyMock.call("1")
            }
        }
    }

    @Test
    fun testVerifiesGenericMethodCalls() {
        assertVerified {
            verify {
                dependencyMock.callGeneric(1)
            }
        }
    }

    @Test
    fun testVerifiesGenericSuspendMethodCalls() {
        assertVerified {
            verifySuspend {
                dependencyMock.callSuspendGeneric(2.0)
            }
        }
    }

    @Test
    fun testVerifiesExtensionMethodCalls() {
        assertVerified {
            verifySuspend {
                dependencyMock.run {
                    listOf("").extension()
                }
            }
        }
    }

    @Test
    fun testVerifiesGenericExtensionMethodCalls() {
        assertVerified {
            verifySuspend {
                dependencyMock.run {
                    listOf("").genericExtension()
                }
            }
        }
    }

    @Test
    fun testVerifiesExtensionPropertyCalls() {
        assertVerified {
            verifySuspend {
                dependencyMock.run {
                    listOf("").listSize
                }
            }
        }
    }

    @Test
    fun testVerifiesGenericExtensionPropertyCalls() {
        assertVerified {
            verifySuspend {
                dependencyMock.run {
                    listOf("").genericListSize
                }
            }
        }
    }
}
