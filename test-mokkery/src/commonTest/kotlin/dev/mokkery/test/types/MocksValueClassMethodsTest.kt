package dev.mokkery.test.types

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.register
import dev.mokkery.answering.autofill.unregister
import dev.mokkery.answering.returns
import dev.mokkery.answering.returnsSuccess
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.test.ComplexType
import dev.mokkery.test.PrimitiveValueClass
import dev.mokkery.test.ValueClass
import dev.mokkery.test.ValueClassMethodsInterface
import dev.mokkery.verify
import kotlin.Result.Companion.success
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MocksValueClassMethodsTest {

    private val mock = mock<ValueClassMethodsInterface<CharSequence>>()

    @BeforeTest
    fun before() {
        AutofillProvider.forInternals.types.register<PrimitiveValueClass> { PrimitiveValueClass(0) }
        AutofillProvider.forInternals.types.register<ValueClass<Nothing?>> { ValueClass(null) }
    }

    @AfterTest
    fun after() {
        AutofillProvider.forInternals.types.unregister<PrimitiveValueClass>()
        AutofillProvider.forInternals.types.unregister<ValueClass<Nothing?>>()
    }

    @Test
    fun testCallPrimitiveResult() {
        every { mock.callPrimitiveResult(any()) } returnsSuccess 1
        assertEquals(success(1), mock.callPrimitiveResult(success(1)))
        verify { mock.callPrimitiveResult(success(1)) }
    }

    @Test
    fun testCallComplexResult() {
        every { mock.callComplexResult(any()) } returnsSuccess ComplexType
        assertEquals(success(ComplexType), mock.callComplexResult(success(ComplexType)))
        verify { mock.callComplexResult(success(ComplexType)) }
    }


    @Test
    fun testCallParentGenericResult() {
        every { mock.callParentGenericResult(any()) } returnsSuccess "Hello!"
        assertEquals(success("Hello!"), mock.callParentGenericResult(success("")))
        verify { mock.callParentGenericResult(success("")) }
    }

    @Test
    fun testCallGenericResult() {
        every { mock.callGenericResult(any<Result<String>>()) } returnsSuccess "Hello!"
        assertEquals(success("Hello!"), mock.callGenericResult(success("")))
        verify { mock.callGenericResult(success("")) }
    }

    @Test
    fun testCallPrimitiveValueClass() {
        every { mock.callPrimitiveValueClass(any()) } returns PrimitiveValueClass(1)
        assertEquals(PrimitiveValueClass(1), mock.callPrimitiveValueClass(PrimitiveValueClass(0)))
        verify { mock.callPrimitiveValueClass(PrimitiveValueClass(0)) }
    }

    @Test
    fun testCallValueClass() {
        every { mock.callValueClass(any()) } returns ValueClass(ComplexType)
        assertEquals(ValueClass<ComplexType>(ComplexType), mock.callValueClass(ValueClass(ComplexType)))
        verify { mock.callValueClass(ValueClass(ComplexType)) }
    }

    @Test
    fun testCallParentGenericValueClass() {
        every { mock.callParentGenericValueClass(any()) } returns ValueClass("Hello!")
        assertEquals(ValueClass<CharSequence>("Hello!"), mock.callParentGenericValueClass(ValueClass("")))
        verify { mock.callParentGenericValueClass(ValueClass("")) }
    }

    @Test
    fun testCallGenericValueClass() {
        every { mock.callGenericValueClass(any<ValueClass<String>>()) } returns ValueClass("Hello!")
        assertEquals(ValueClass("Hello!"), mock.callGenericValueClass(ValueClass("")))
        verify { mock.callGenericValueClass(ValueClass("")) }
    }
}
