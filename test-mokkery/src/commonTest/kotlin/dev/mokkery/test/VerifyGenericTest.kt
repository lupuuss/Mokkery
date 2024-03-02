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
    fun testVerifiesBoundedGenericMethodCalls() {
        assertVerified {
            verify {
                dependencyMock.callBoundedGeneric(1)
            }
        }
    }

    @Test
    @IgnoreOnWasmWasi
    fun testVerifiesBoundedGenericSuspendMethodCalls() {
        assertVerified {
            verifySuspend {
                dependencyMock.callSuspendBoundedGeneric(2.0)
            }
        }
    }

    @Test
    fun testVerifiesExtensionMethodCalls() {
        assertVerified {
            verify {
                dependencyMock.run {
                    listOf("").extension()
                }
            }
        }
    }

    @Test
    fun testVerifiesGenericExtensionMethodCalls() {
        assertVerified {
            verify {
                dependencyMock.run {
                    listOf("").genericExtension()
                }
            }
        }
    }

    @Test
    fun testVerifiesExtensionPropertyCalls() {
        assertVerified {
            verify {
                dependencyMock.run {
                    listOf("").listSize
                }
            }
        }
    }

    @Test
    fun testVerifiesGenericExtensionPropertyCalls() {
        assertVerified {
            verify {
                dependencyMock.run {
                    listOf("").genericListSize
                }
            }
        }
    }
}
