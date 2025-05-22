package dev.mokkery.test.answers

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.Answer
import dev.mokkery.answering.FunctionScope
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.test.ComplexType
import dev.mokkery.test.DefaultsInterfaceLevel1
import dev.mokkery.test.RegularMethodsInterface
import dev.mokkery.test.SuspendMethodsInterface
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(DelicateMokkeryApi::class)
class DeprecatedAnswerApiTest {

    @Test
    fun testOldCallForBlockingCall() {
        val mock = mock<RegularMethodsInterface> {
            every { callPrimitive(any()) } answers DeprecatedAnswer { it.arg(0) }
        }
        assertEquals(2, mock.callPrimitive(2))
    }

    @Test
    fun testOldCallForSuspendCall() = runTest {
        val mock = mock<SuspendMethodsInterface> {
            everySuspend { callPrimitive(any()) } answers DeprecatedAnswer { it.arg(0) }
        }
        assertEquals(3, mock.callPrimitive(3))
    }

    @Test
    fun testCoreFunctionScope() {
        val mock = mock<RegularMethodsInterface>()
        every { mock.callUnit(Unit) } answers DeprecatedAnswer {
            assertEquals(mock, it.self)
            assertEquals(Unit::class, it.returnType)
            assertEquals(listOf(Unit), it.args)
            assertEquals(Unit, it.arg(0))
        }
        mock.callUnit(Unit)
    }

    @Test
    fun testSuperCallsFunctionScope() {
        val mock = mock<DefaultsInterfaceLevel1<Int>> {
            every { call(1, any()) } answers DeprecatedAnswer {
                it.callOriginal(it.args) as ComplexType
            }
            every { call(2, any()) } answers DeprecatedAnswer {
                it.callSuper(DefaultsInterfaceLevel1::class, it.args) as ComplexType
            }
        }
        assertEquals(ComplexType("1"), mock.call(1, ComplexType))
        assertEquals(ComplexType("2"), mock.call(2, ComplexType))
    }

    @Test
    fun testSuspendSuperCallsFunctionScope() = runTest {
        val mock = mock<DefaultsInterfaceLevel1<Int>> {
            everySuspend { callSuspend(1, any()) } answers DeprecatedSuspendAnswer {
                it.callSuspendOriginal(it.args) as ComplexType
            }
            everySuspend { callSuspend(2, any()) } answers DeprecatedSuspendAnswer {
                it.callSuspendSuper(DefaultsInterfaceLevel1::class, it.args) as ComplexType
            }
        }
        assertEquals(ComplexType("1"), mock.callSuspend(1, ComplexType))
        assertEquals(ComplexType("2"), mock.callSuspend(2, ComplexType))
    }
}


@Suppress("OVERRIDE_DEPRECATION")
@OptIn(DelicateMokkeryApi::class)
private data class DeprecatedAnswer<T>(val block: (FunctionScope) -> T) : Answer<T> {

    override fun call(scope: FunctionScope): T = block(scope)
}

@Suppress("OVERRIDE_DEPRECATION")
@OptIn(DelicateMokkeryApi::class)
private data class DeprecatedSuspendAnswer<T>(val block: suspend (FunctionScope) -> T) : Answer.Suspending<T> {

    override suspend fun callSuspend(scope: FunctionScope): T = block(scope)
}

