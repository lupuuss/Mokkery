package dev.mokkery.test.types

import dev.mokkery.answering.returnsArgAt
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.templating.ctx
import dev.mokkery.templating.ext
import dev.mokkery.test.ComplexType
import dev.mokkery.test.ContextParametersInterface
import dev.mokkery.test.assertVerified
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class ContextParametersTypesTest {

    private val mock = mock<ContextParametersInterface>()

    @Test
    fun testExtInCtx() {
        every { ctx(any<ComplexType>(), any<Int>()) { mock.ext { any<String>().call() } } } returnsArgAt 2
        assertEquals("1", context(ComplexType, 1) { mock.run { "1".call() } })
        assertEquals("2", context(ComplexType, 1) { mock.run { "2".call() } })
        verify {
            val typeMatcher = any<ComplexType>()
            ctx(typeMatcher, any<Int>()) {
                mock.ext {
                    "1".call()
                    "2".call()
                }
            }
        }
        assertVerified {
            verify {
                val typeMatcher = any<ComplexType>()
                ctx(typeMatcher, any<Int>()) {
                    mock.ext {
                        "3".call()
                        "4".call()
                    }
                }
            }
        }
    }

    @Test
    fun testCtxInExt() {
        every {
            mock.ext {
                ctx(any<ComplexType>(), any<Int>()) { any<String>().call() }
            }
        } returnsArgAt 2
        assertEquals("1", context(ComplexType, 1) { mock.run { "1".call() } })
        assertEquals("2", context(ComplexType, 1) { mock.run { "2".call() } })
        verify {
            mock.ext {
                val typeMatcher = any<ComplexType>()
                ctx(typeMatcher, any<Int>()) {
                    "1".call()
                    "2".call()
                }
            }
        }
        assertVerified {
            verify {
                mock.ext {
                    val typeMatcher = any<ComplexType>()
                    ctx(typeMatcher, any<Int>()) {
                        "3".call()
                        "4".call()
                    }
                }
            }
        }
    }
}
