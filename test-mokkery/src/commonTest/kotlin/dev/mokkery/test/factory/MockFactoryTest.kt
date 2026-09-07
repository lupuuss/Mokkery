@file:OptIn(DelicateMokkeryApi::class)

package dev.mokkery.test.factory

import dev.mokkery.MockMode.autoUnit
import dev.mokkery.MockMode.autofill
import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.MokkerySuiteScope
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.factory.create
import dev.mokkery.factory.createOrNull
import dev.mokkery.factory.defaultMockMode
import dev.mokkery.factory.mockFactoryOf
import dev.mokkery.factory.plus
import dev.mokkery.matcher.any
import dev.mokkery.mockMode
import dev.mokkery.presets.preset
import dev.mokkery.test.ComplexArgsInterface
import dev.mokkery.test.GenericFunctionsInterface
import dev.mokkery.test.RegularMethodsInterface
import dev.mokkery.test.SuspendMethodsInterface
import dev.mokkery.test.assertVerified
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exhaustive
import kotlinx.coroutines.test.runTest
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MockFactoryTest {

    private val factory = mockFactoryOf(
        RegularMethodsInterface::class,
        SuspendMethodsInterface::class,
        List::class,
    ) {
        defaultMockMode = autoUnit
    }

    @Test
    fun createsMockOfSelectedTypes() = runTest {
        val mock1 = factory.create<RegularMethodsInterface>()
        val mock2 = factory.create<SuspendMethodsInterface>()
        every { mock1.callPrimitive(any()) } returns 1
        everySuspend { mock2.callPrimitive(any()) } returns 2
        assertEquals(1, mock1.callPrimitive(1))
        assertEquals(2, mock2.callPrimitive(2))
    }

    @Test
    fun returnsNullForUnregisteredType() {
        assertNull(factory.createOrNull(typeOf<ComplexArgsInterface>()))
    }

    @Test
    fun createsMockOfReifiedTypeAndAppliesBlock() {
        val mock = factory.createOrNull<RegularMethodsInterface> {
            every { callPrimitive(any()) } returns 1
        }
        assertEquals(1, assertNotNull(mock).callPrimitive(1))
    }

    @Test
    fun returnsNullForUnregisteredReifiedType() {
        assertNull(factory.createOrNull<ComplexArgsInterface>())
    }

    @Test
    fun appliesDefaultMode() {
        val mock = factory.create<RegularMethodsInterface>()
        mock.callUnit(Unit)
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(0) }
    }

    @Test
    fun appliesModeArgumentSpecifiedOnCreate() {
        val mock = factory.create<RegularMethodsInterface> { mockMode = autofill }
        assertEquals(0, mock.callPrimitive(1))
    }

    @Test
    fun usesExplicitScope() = with(MokkerySuiteScope()) {
        val factory = mockFactoryOf(RegularMethodsInterface::class)
        val mock1 = factory.create<RegularMethodsInterface>()
        val mock2 = factory.create<RegularMethodsInterface>()
        every { mock1.callPrimitive(any()) } returns 1
        everySuspend { mock2.callPrimitive(any()) } returns 2
        assertEquals(1, mock1.callPrimitive(1))
        assertEquals(2, mock2.callPrimitive(2))
        assertVerified { verify(exhaustive) { mock1.callPrimitive(any()) } }
    }

    @Test
    fun combinedFactoryCreatesMocksFromBothFactories() = runTest {
        val factory = mockFactoryOf(RegularMethodsInterface::class) +
            mockFactoryOf(SuspendMethodsInterface::class)
        val mock1 = factory.create<RegularMethodsInterface>()
        val mock2 = factory.create<SuspendMethodsInterface>()
        every { mock1.callPrimitive(any()) } returns 1
        everySuspend { mock2.callPrimitive(any()) } returns 2
        assertEquals(1, mock1.callPrimitive(1))
        assertEquals(2, mock2.callPrimitive(2))
        assertNull(factory.createOrNull(typeOf<ComplexArgsInterface>()))
    }

    @Test
    fun combinedFactoryCopyAppliesConfigurationToBothFactories() = runTest {
        val factory = mockFactoryOf(RegularMethodsInterface::class) +
            mockFactoryOf(SuspendMethodsInterface::class)
        val newFactory = factory.copy { defaultMockMode = autofill }
        assertEquals(0, newFactory.create<RegularMethodsInterface>().callPrimitive(1))
        assertEquals(0, newFactory.create<SuspendMethodsInterface>().callPrimitive(2))
    }

    @Test
    fun combinedFactoryCopyWithNewScopeAssociatesBothFactoriesWithThatScope() = runTest {
        val factory = mockFactoryOf(RegularMethodsInterface::class) +
            mockFactoryOf(SuspendMethodsInterface::class)
        with(MokkerySuiteScope()) {
            val newFactory = factory.copy(this) { defaultMockMode = autofill }
            val mock1 = newFactory.create<RegularMethodsInterface>()
            val mock2 = newFactory.create<SuspendMethodsInterface>()
            mock1.callPrimitive(1)
            mock2.callPrimitive(2)
            assertVerified { verify(exhaustive) { mock1.callPrimitive(any()) } }
        }
    }

    @Test
    fun copyCreatesFactoryWithAppliedConfiguration() {
        val factory = mockFactoryOf(RegularMethodsInterface::class) { defaultMockMode = autoUnit }
        val newFactory = factory.copy { defaultMockMode = autofill }
        newFactory
            .create<RegularMethodsInterface>()
            .callPrimitive(0)
    }

    @Test
    fun createsMockOfTypeWithMultipleTypeParameters() {
        val factory = mockFactoryOf(Map::class)
        val mock = factory.create<Map<String, Int>> { mockMode = autofill }
        assertEquals(0, mock.size)
    }

    @Test
    fun copyWithNewScopeCreatesFactoryAssociatedWithThatScope() {
        val factory = mockFactoryOf(RegularMethodsInterface::class)
        with(MokkerySuiteScope()) {
            val newFactory = factory.copy(this) { defaultMockMode = autofill }
            val mock1 = newFactory.create<RegularMethodsInterface>()
            val mock2 = newFactory.create<RegularMethodsInterface>()
            mock1.callPrimitive(1)
            mock2.callPrimitive(2)
            assertVerified { verify(exhaustive) { mock1.callPrimitive(any()) } }
        }
    }

    @Test
    fun appliesPresets() {
        val factory = mockFactoryOf(RegularMethodsInterface::class) {
            preset<RegularMethodsInterface> {
                every { callPrimitive(any()) } returns 1
            }
        }
        assertEquals(1, factory.create<RegularMethodsInterface>().callPrimitive(0))
    }

    @Test
    fun appliesPresetsBeforeCreationBlock() {
        val factory = mockFactoryOf(RegularMethodsInterface::class) {
            preset<RegularMethodsInterface> {
                every { callPrimitive(any()) } returns 1
                every { callBoolean(any()) } returns true
            }
        }
        val mock = factory.create<RegularMethodsInterface> { every { callPrimitive(any()) } returns 2 }
        assertEquals(2, mock.callPrimitive(0))
        assertEquals(true, mock.callBoolean(false))
    }

    @Test
    fun appliesAllPresetsForSameType() {
        val factory = mockFactoryOf(RegularMethodsInterface::class) {
            defaultMockMode = autofill
            preset<RegularMethodsInterface> {
                every { callBoolean(any()) } returns true
                every { callPrimitive(any()) } returns 1
            }
            preset<RegularMethodsInterface> { every { callPrimitive(any()) } returns 2 }
        }
        val mock = factory.create<RegularMethodsInterface>()
        assertEquals(2, mock.callPrimitive(0))
        assertEquals(true, mock.callBoolean(false))
    }

    @Test
    fun appliesPresetsByTypeArguments() {
        val factory = mockFactoryOf(GenericFunctionsInterface::class) {
            defaultMockMode = autofill
            preset<GenericFunctionsInterface<String>> { every { call(any()) } returns "1" }
        }
        assertEquals("1", factory.create<GenericFunctionsInterface<String>>().call("0"))
        assertEquals(0, factory.create<GenericFunctionsInterface<Int>>().call(0))
    }

    @Test
    fun appliesStarProjectedPresetsToAllTypeArguments() {
        val factory = mockFactoryOf(GenericFunctionsInterface::class) {
            preset<GenericFunctionsInterface<*>> { every { callGenericWithStarProjection(any()) } returns listOf(1) }
        }
        assertEquals(listOf(1), factory.create<GenericFunctionsInterface<String>>().callGenericWithStarProjection(listOf<Any>()))
        assertEquals(listOf(1), factory.create<GenericFunctionsInterface<Int>>().callGenericWithStarProjection(listOf<Any>()))
        assertEquals(listOf(1), factory.create<GenericFunctionsInterface<*>>().callGenericWithStarProjection(listOf<Any>()))
    }

    @Test
    fun appliesPresetsFromLeastToMostSpecificMatch() {
        val applied = mutableListOf<String>()
        val factory = mockFactoryOf(GenericFunctionsInterface::class) {
            preset<GenericFunctionsInterface<String>> {
                applied += "exact"
                every { callGenericWithStarProjection(any()) } returns listOf(2)
            }
            preset<GenericFunctionsInterface<*>> {
                applied += "star"
                every { callGenericWithStarProjection(any()) } returns listOf(1)
            }
        }
        val exact = factory.create<GenericFunctionsInterface<String>>()
        val star = factory.create<GenericFunctionsInterface<CharSequence>>()
        assertEquals(listOf("star", "exact", "star"), applied)
        assertEquals(listOf(2), exact.callGenericWithStarProjection(listOf<Any>()))
        assertEquals(listOf(1), star.callGenericWithStarProjection(listOf<Any>()))
    }

    @Test
    fun appliesEquallySpecificPresetsInRegistrationOrder() {
        val applied = mutableListOf<String>()
        val factory = mockFactoryOf(Map::class) {
            defaultMockMode = autofill
            preset<Map<String, *>> { applied += "first" }
            preset<Map<*, Int>> { applied += "second" }
            preset<Map<String, *>> { applied += "third" }
        }
        factory.create<Map<String, Int>>()
        assertEquals(listOf("first", "second", "third"), applied)
    }

    @Test
    fun copyPreservesPresets() {
        val factory = mockFactoryOf(RegularMethodsInterface::class) {
            preset<RegularMethodsInterface> { every { callPrimitive(any()) } returns 1 }
        }
        assertEquals(1, factory.copy().create<RegularMethodsInterface>().callPrimitive(0))
    }

    @Test
    fun copyAppliesInheritedPresetsBeforeRegisteredOnes() {
        val factory = mockFactoryOf(RegularMethodsInterface::class) {
            defaultMockMode = autofill
            preset<RegularMethodsInterface> {
                every { callPrimitive(any()) } returns 1
                every { callBoolean(any()) } returns true
            }
        }
        val mock = factory
            .copy { preset<RegularMethodsInterface> { every { callPrimitive(any()) } returns 2 } }
            .create<RegularMethodsInterface>()
        assertEquals(2, mock.callPrimitive(0))
        assertEquals(true, mock.callBoolean(false))
    }

    @Test
    fun copyDoesNotShareRegisteredPresetsWithOriginalFactory() {
        val factory = mockFactoryOf(RegularMethodsInterface::class) {
            preset<RegularMethodsInterface> { every { callPrimitive(any()) } returns 1 }
        }
        val newFactory = factory.copy { preset<RegularMethodsInterface> { every { callPrimitive(any()) } returns 2 } }
        assertEquals(1, factory.create<RegularMethodsInterface>().callPrimitive(0))
        assertEquals(2, newFactory.create<RegularMethodsInterface>().callPrimitive(0))
    }

    @Test
    fun copyWithNewScopeRejectsPresets() = with(MokkerySuiteScope()) {
        val factory = mockFactoryOf(RegularMethodsInterface::class) {
            preset<RegularMethodsInterface> { every { callPrimitive(any()) } returns 1 }
        }
        val newFactory = factory.copy(this) { defaultMockMode = autofill }
        assertEquals(0, newFactory.create<RegularMethodsInterface>().callPrimitive(0))
    }

    @Test
    fun ignoresPresetsOfUnsupportedTypes() {
        val factory = mockFactoryOf(RegularMethodsInterface::class) {
            defaultMockMode = autofill
            preset<SuspendMethodsInterface> { everySuspend { callPrimitive(any()) } returns 1 }
        }
        assertNull(factory.createOrNull<SuspendMethodsInterface>())
        assertEquals(0, factory.create<RegularMethodsInterface>().callPrimitive(0))
    }
}
