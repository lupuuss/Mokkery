package dev.mokkery.test.factory

import dev.mokkery.MokkerySuiteScope
import dev.mokkery.answering.returns
import dev.mokkery.call
import dev.mokkery.every
import dev.mokkery.factory.create
import dev.mokkery.factory.createOrNull
import dev.mokkery.factory.plus
import dev.mokkery.factory.spyFactoryOf
import dev.mokkery.interceptor.callHooks
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.presets.preset
import dev.mokkery.test.ComplexArgsInterface
import dev.mokkery.test.ComplexType
import dev.mokkery.test.SpyTestInterface
import dev.mokkery.test.assertVerified
import dev.mokkery.test.interceptor.TestInterceptor
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exhaustive
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SpyFactoryTest {

    private val factory = spyFactoryOf(
        SpyTestInterface::class,
        List::class,
    )

    @Test
    fun createsSpyOfSelectedTypes() {
        val obj = SpyTestInterface.Companion<Int>()
        val spy = factory.create<SpyTestInterface<Int>>(obj)
        every { spy.property } returns 33
        assertEquals(obj.call(ComplexType.Companion("1")), spy.call(ComplexType.Companion("1")))
        assertEquals(33, spy.property)
    }

    @Test
    fun returnsNullForUnregisteredType() {
        assertNull(factory.createOrNull(typeOf<ComplexArgsInterface>(), listOf(1)))
    }

    @Test
    fun createsSpyOfReifiedTypeAndAppliesBlock() {
        val obj = SpyTestInterface.Companion<Int>()
        val spy = factory.createOrNull<SpyTestInterface<Int>>(obj) {
            every { property } returns 33
        }
        assertEquals(33, assertNotNull(spy).property)
        assertEquals(obj.call(ComplexType.Companion("1")), spy.call(ComplexType.Companion("1")))
    }

    @Test
    fun returnsNullForUnregisteredReifiedType() {
        assertNull(factory.createOrNull<ComplexArgsInterface>(mock()))
    }

    @Test
    fun usesExplicitScope() = with(MokkerySuiteScope()) {
        val factory = spyFactoryOf(SpyTestInterface::class, List::class,)
        val spy1 = factory.create(listOf(1))
        val spy2 = factory.create(listOf(2))
        assertEquals(1, spy1[0])
        assertEquals(2, spy2[0])
        assertVerified { verify(exhaustive) { spy1[0] } }
    }

    @Test
    fun combinedFactoryCreatesSpiesFromBothFactories() {
        val factory = spyFactoryOf(SpyTestInterface::class) + spyFactoryOf(List::class)
        val obj = SpyTestInterface.Companion<Int>()
        val spy1 = factory.create<SpyTestInterface<Int>>(obj)
        val spy2 = factory.create(listOf(1))
        every { spy1.property } returns 33
        assertEquals(33, spy1.property)
        assertEquals(1, spy2[0])
        assertNull(factory.createOrNull(typeOf<ComplexArgsInterface>(), listOf(1)))
    }

    @Test
    fun combinedFactoryCopyAppliesConfigurationToBothFactories() {
        val interceptor = TestInterceptor()
        val factory = spyFactoryOf(SpyTestInterface::class) + spyFactoryOf(List::class)
        val newFactory = factory.copy { callHooks.beforeAnswering.register(interceptor) }
        val spy1 = newFactory.create<SpyTestInterface<Int>>(SpyTestInterface.Companion())
        val spy2 = newFactory.create(listOf(1))
        spy1.call(ComplexType.Companion("1"))
        spy2[0]
        assertNotNull(
            interceptor
                .interceptBlockingCalls
                .singleOrNull { it.call.function.name == "call" }
        )
        assertNotNull(
            interceptor
                .interceptBlockingCalls
                .singleOrNull { it.call.function.name == "get" }
        )
    }

    @Test
    fun combinedFactoryCopyWithNewScopeAssociatesBothFactoriesWithThatScope() {
        val factory = spyFactoryOf(SpyTestInterface::class) + spyFactoryOf(List::class)
        with(MokkerySuiteScope()) {
            val newFactory = factory.copy(this)
            val spy1 = newFactory.create<SpyTestInterface<Int>>(SpyTestInterface.Companion())
            val spy2 = newFactory.create(listOf(1))
            spy1.call(ComplexType.Companion("1"))
            spy2[0]
            assertVerified { verify(exhaustive) { spy1.call(any()) } }
        }
    }

    @Test
    fun appliesPresets() {
        val factory = spyFactoryOf(SpyTestInterface::class) {
            preset<SpyTestInterface<Int>> {
                every { property } returns 1
            }
        }
        assertEquals(1, factory.create<SpyTestInterface<Int>>(SpyTestInterface.Companion()).property)
    }

    @Test
    fun appliesPresetsBeforeCreationBlock() {
        val factory = spyFactoryOf(SpyTestInterface::class) {
            preset<SpyTestInterface<Int>> {
                every { property } returns 1
                every { call(any()) } returns ComplexType.Companion("9")
            }
        }
        val spy = factory.create<SpyTestInterface<Int>>(SpyTestInterface.Companion()) {
            every { property } returns 2
        }
        assertEquals(2, spy.property)
        assertEquals(ComplexType.Companion("9"), spy.call(ComplexType.Companion("1")))
    }

    @Test
    fun appliesAllPresetsForSameType() {
        val factory = spyFactoryOf(SpyTestInterface::class) {
            preset<SpyTestInterface<Int>> {
                every { call(any()) } returns ComplexType.Companion("9")
                every { property } returns 1
            }
            preset<SpyTestInterface<Int>> { every { property } returns 2 }
        }
        val spy = factory.create<SpyTestInterface<Int>>(SpyTestInterface.Companion())
        assertEquals(2, spy.property)
        assertEquals(ComplexType.Companion("9"), spy.call(ComplexType.Companion("1")))
    }

    @Test
    fun appliesPresetsByTypeArguments() {
        val factory = spyFactoryOf(SpyTestInterface::class) {
            preset<SpyTestInterface<Int>> { every { property } returns 1 }
        }
        assertEquals(1, factory.create<SpyTestInterface<Int>>(SpyTestInterface.Companion()).property)
        assertNull(factory.create<SpyTestInterface<String>>(SpyTestInterface.Companion()).property)
    }

    @Test
    fun appliesStarProjectedPresetsToAllTypeArguments() {
        val factory = spyFactoryOf(SpyTestInterface::class) {
            preset<SpyTestInterface<*>> { every { call(any()) } returns ComplexType.Companion("9") }
        }
        val spy1 = factory.create<SpyTestInterface<Int>>(SpyTestInterface.Companion())
        val spy2 = factory.create<SpyTestInterface<String>>(SpyTestInterface.Companion())
        val spy3 = factory.create<SpyTestInterface<*>>(SpyTestInterface.Companion<Int>())
        assertEquals(ComplexType.Companion("9"), spy1.call(ComplexType.Companion("1")))
        assertEquals(ComplexType.Companion("9"), spy2.call(ComplexType.Companion("1")))
        assertEquals(ComplexType.Companion("9"), spy3.call(ComplexType.Companion("1")))
    }

    @Test
    fun appliesPresetsFromLeastToMostSpecificMatch() {
        val applied = mutableListOf<String>()
        val factory = spyFactoryOf(SpyTestInterface::class) {
            preset<SpyTestInterface<Int>> {
                applied += "exact"
                every { call(any()) } returns ComplexType.Companion("8")
            }
            preset<SpyTestInterface<*>> {
                applied += "star"
                every { call(any()) } returns ComplexType.Companion("9")
            }
        }
        val exact = factory.create<SpyTestInterface<Int>>(SpyTestInterface.Companion())
        val star = factory.create<SpyTestInterface<String>>(SpyTestInterface.Companion())
        assertEquals(listOf("star", "exact", "star"), applied)
        assertEquals(ComplexType.Companion("8"), exact.call(ComplexType.Companion("1")))
        assertEquals(ComplexType.Companion("9"), star.call(ComplexType.Companion("1")))
    }

    @Test
    fun copyPreservesPresets() {
        val factory = spyFactoryOf(SpyTestInterface::class) {
            preset<SpyTestInterface<Int>> { every { property } returns 1 }
        }
        assertEquals(1, factory.copy().create<SpyTestInterface<Int>>(SpyTestInterface.Companion()).property)
    }

    @Test
    fun copyAppliesInheritedPresetsBeforeRegisteredOnes() {
        val factory = spyFactoryOf(SpyTestInterface::class) {
            preset<SpyTestInterface<Int>> {
                every { property } returns 1
                every { call(any()) } returns ComplexType.Companion("9")
            }
        }
        val spy = factory
            .copy { preset<SpyTestInterface<Int>> { every { property } returns 2 } }
            .create<SpyTestInterface<Int>>(SpyTestInterface.Companion())
        assertEquals(2, spy.property)
        assertEquals(ComplexType.Companion("9"), spy.call(ComplexType.Companion("1")))
    }

    @Test
    fun copyDoesNotShareRegisteredPresetsWithOriginalFactory() {
        val factory = spyFactoryOf(SpyTestInterface::class) {
            preset<SpyTestInterface<Int>> { every { property } returns 1 }
        }
        val newFactory = factory.copy { preset<SpyTestInterface<Int>> { every { property } returns 2 } }
        assertEquals(1, factory.create<SpyTestInterface<Int>>(SpyTestInterface.Companion()).property)
        assertEquals(2, newFactory.create<SpyTestInterface<Int>>(SpyTestInterface.Companion()).property)
    }

    @Test
    fun copyWithNewScopeRejectsPresets() = with(MokkerySuiteScope()) {
        val factory = spyFactoryOf(SpyTestInterface::class) {
            preset<SpyTestInterface<Int>> { every { property } returns 1 }
        }
        val newFactory = factory.copy(this)
        assertNull(newFactory.create<SpyTestInterface<Int>>(SpyTestInterface.Companion()).property)
    }

    @Test
    fun ignoresPresetsOfUnsupportedTypes() {
        val factory = spyFactoryOf(SpyTestInterface::class) {
            preset<List<Int>> { every { size } returns 1 }
        }
        assertNull(factory.createOrNull(typeOf<List<Int>>(), listOf(1)))
        assertEquals(ComplexType.Companion("2"), factory.create<SpyTestInterface<Int>>(SpyTestInterface.Companion()).call(ComplexType.Companion("1")))
    }
}
