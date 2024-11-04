package dev.mokkery.test.autofill

import dev.mokkery.MockMode.autofill
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.test.RegularMethodsInterface
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(DelicateMokkeryApi::class)
class AutofillProviderTest {

    private val mock = mock<RegularMethodsInterface>(autofill)
    private val forMockMode = AutofillProvider.ofNotNull {
        when (it) {
            String::class -> "Hello!"
            Int::class -> 2
            else -> null
        }
    }
    private val forInternals = AutofillProvider.ofNotNull {
        when (it) {
            Int::class -> 1
            Double::class -> 1.0
            else -> null
        }
    }

    @BeforeTest
    fun before() {
        AutofillProvider.forMockMode.delegates.register(forMockMode)
        AutofillProvider.forInternals.delegates.register(forInternals)
    }

    @AfterTest
    fun after() {
        AutofillProvider.forMockMode.delegates.unregister(forMockMode)
        AutofillProvider.forInternals.delegates.unregister(forInternals)
    }

    @Test
    fun testProvidesAutofillValuesForMockModeFromBothProvidersPrioritizingForMock() {
        assertEquals(2, mock.callOverloaded(0))
        assertEquals(1.0, mock.callOverloaded(0.0))
        assertEquals("Hello!", mock.callOverloaded(""))
    }

    @Test
    fun testProvidesAutofillValuesForInternalsOnlyFromDedicatedProvider() {
        var internalsValueInt: Int? = null
        var internalsValueDouble: Double? = null
        every { mock.callOverloaded(any<Int>()).also { internalsValueInt = it } } returns 33
        every { mock.callOverloaded(any<Double>()).also { internalsValueDouble = it } } returns 33.0
        assertEquals(1, internalsValueInt)
        assertEquals(1.0, internalsValueDouble)
    }

    @Test
    fun testProperlyUnregistersProviders() {
        AutofillProvider.forMockMode.delegates.unregister(forMockMode)
        AutofillProvider.forInternals.delegates.unregister(forInternals)
        assertEquals(0, mock.callOverloaded(0))
        assertEquals(0.0, mock.callOverloaded(0.0))
        assertEquals("", mock.callOverloaded(""))
    }

}
