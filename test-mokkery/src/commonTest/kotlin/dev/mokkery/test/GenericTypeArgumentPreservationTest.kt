package dev.mokkery.test

import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.context.Function
import dev.mokkery.every
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.MokkeryCallListener
import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.interceptor.call
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlin.reflect.KClass
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GenericTypeArgumentPreservationTest {

    private var capturedReturnType: KClass<*>? = null
    private var capturedArgumentTypes: List<*>? = null
    private val listener = object : MokkeryCallListener {
        override fun onIntercept(scope: MokkeryCallScope) {
            capturedReturnType = scope.call.function.returnType
            capturedArgumentTypes = scope.call.function.parameters.map(Function.Parameter::type)
        }
    }

    @BeforeTest
    fun before() {
        MokkeryCallInterceptor.beforeAnswering.register(listener)
    }

    @AfterTest
    fun after() {
        MokkeryCallInterceptor.beforeAnswering.unregister(listener)
    }

    @Test
    fun testPreservesRegularGenericArgumentForInterface() {
        mock<GenericFunctionsInterface<Int>> { every { call(any()) } returns 10 }
            .call(10)
        assertEquals(Int::class, capturedReturnType)
        assertEquals(listOf(Int::class), capturedArgumentTypes)
    }


    @Test
    fun testPreservesRegularGenericArgumentForFunction() {
        mock<(Int, Double) -> String> {
            every { invoke(any(), any()) } returns ""
        }.invoke(1, 1.0)
        assertEquals(String::class, capturedReturnType)
        assertEquals(listOf(Int::class, Double::class), capturedArgumentTypes)
    }

    @Test
    fun testPreservesStarProjectionToAny() {
        val mock = mock<List<*>> {
            every { get(any()) } returns ComplexType
            every { contains(any()) } returns true
        }
        mock[0]
        assertEquals(Any::class, capturedReturnType)
        assertEquals(listOf(Int::class), capturedArgumentTypes)
        mock.contains(ComplexType)
        assertEquals(Boolean::class, capturedReturnType)
        assertEquals(listOf(Any::class), capturedArgumentTypes)
    }

    @Test
    fun testPreservesGenericParameterToAnyWhenNoBounds() {
        fun <T> mockList() = mock<List<T>>()
        val mock = mockList<Any?>().apply {
            every { get(any()) } returns 1
            every { contains(any()) } returns false
        }
        mock[0]
        assertEquals(Any::class, capturedReturnType)
        assertEquals(listOf(Int::class), capturedArgumentTypes)
        mock.contains(null)
        assertEquals(Boolean::class, capturedReturnType)
        assertEquals(listOf(Any::class), capturedArgumentTypes)
    }

    @Test
    fun testPreservesGenericParameterToBoundWhenPresent() {
        fun <T : CharSequence> mockList() = mock<List<T>>()
        val mock = mockList<String>().apply {
            every { get(any()) } returns "1"
            every { contains(any()) } returns false
        }
        mock[0]
        assertEquals(CharSequence::class, capturedReturnType)
        assertEquals(listOf(Int::class), capturedArgumentTypes)
        mock.contains("")
        assertEquals(Boolean::class, capturedReturnType)
        assertEquals(listOf(CharSequence::class), capturedArgumentTypes)
    }
}
