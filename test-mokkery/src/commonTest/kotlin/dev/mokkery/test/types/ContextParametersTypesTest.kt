package dev.mokkery.test.types

import dev.mokkery.answering.calls
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
        every { ctx(any<ComplexType>(), any<Int>()) { mock.ext { any<String>().callCtxAndExt() } } } returnsArgAt 2
        assertEquals("1", context(ComplexType, 1) { mock.run { "1".callCtxAndExt() } })
        assertEquals("2", context(ComplexType, 1) { mock.run { "2".callCtxAndExt() } })
        verify {
            val typeMatcher = any<ComplexType>()
            ctx(typeMatcher, any<Int>()) {
                mock.ext {
                    "1".callCtxAndExt()
                    "2".callCtxAndExt()
                }
            }
        }
        assertVerified {
            verify {
                val typeMatcher = any<ComplexType>()
                ctx(typeMatcher, any<Int>()) {
                    mock.ext {
                        "3".callCtxAndExt()
                        "4".callCtxAndExt()
                    }
                }
            }
        }
    }

    @Test
    fun testCtxInExt() {
        every {
            mock.ext {
                ctx(any<ComplexType>(), any<Int>()) { any<String>().callCtxAndExt() }
            }
        } returnsArgAt 2
        assertEquals("1", context(ComplexType, 1) { mock.run { "1".callCtxAndExt() } })
        assertEquals("2", context(ComplexType, 1) { mock.run { "2".callCtxAndExt() } })
        verify {
            mock.ext {
                val typeMatcher = any<ComplexType>()
                ctx(typeMatcher, any<Int>()) {
                    "1".callCtxAndExt()
                    "2".callCtxAndExt()
                }
            }
        }
        assertVerified {
            verify {
                mock.ext {
                    val typeMatcher = any<ComplexType>()
                    ctx(typeMatcher, any<Int>()) {
                        "3".callCtxAndExt()
                        "4".callCtxAndExt()
                    }
                }
            }
        }
    }

    @Test
    fun testExt() {
        every { mock.ext { any<ComplexType>().callExt() } } calls { (type: ComplexType) ->
            type.toString()
        }
        assertEquals("ComplexTypeImpl(id=1)", mock.run { ComplexType("1").callExt() })
        assertVerified { verify { mock.ext { ComplexType("2").callExt() } } }
        verify { mock.ext { ComplexType("1").callExt() } }
    }

    @Test
    fun testCtx() {
        every { ctx(any<ComplexType>(), any<Int>()) { mock.callCtx() } } calls { (type: ComplexType, i: Int) ->
            "$type + $i"
        }
        assertEquals("ComplexTypeImpl(id=1) + 2", context(ComplexType("1"), 2) { mock.callCtx() })
        assertVerified { verify { ctx(ComplexType("2"), 2) { mock.callCtx() } } }
        verify { ctx(ComplexType("1"), 2) { mock.callCtx() } }
    }
}
