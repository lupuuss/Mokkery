package dev.mokkery.test

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.MokkeryScope
import dev.mokkery.MokkerySuiteScope
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.configurer.MokkeryMockConfigurer
import dev.mokkery.configurer.configurer
import dev.mokkery.configurer.minusAssign
import dev.mokkery.configurer.plusAssign
import dev.mokkery.context.MokkeryContext
import dev.mokkery.interceptor.MokkeryCallHooks
import dev.mokkery.mock
import dev.mokkery.mockMany
import dev.mokkery.mockMode
import dev.mokkery.mocks
import dev.mokkery.spy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@OptIn(DelicateMokkeryApi::class)
class MockConfigurerTest {

    @Test
    fun testMockBlockAllowsMutatingContext() {
        val mock = mock<RegularMethodsInterface> {
            configurer += TestElement("mock")
        }
        assertEquals(TestElement("mock"), MokkeryScope.from(mock).mokkeryContext[TestElement])
    }

    @Test
    fun testSpyBlockAllowsMutatingContext() {
        val spied = spy(listOf(1, 2, 3)) {
            configurer += TestElement("spy")
        }
        assertEquals(TestElement("spy"), MokkeryScope.from(spied).mokkeryContext[TestElement])
    }

    @Test
    fun testMockManyBlockAllowsMutatingContext() {
        val mock = mockMany<RegularMethodsInterface, AutoCloseable> {
            configurer += TestElement("mockMany")
        }
        assertEquals(TestElement("mockMany"), MokkeryScope.from(mock).mokkeryContext[TestElement])
    }

    @Test
    fun testFunctionMockBlockAllowsMutatingContext() {
        val mock = mock<() -> Unit> {
            configurer += TestElement("function")
        }
        assertEquals(TestElement("function"), MokkeryScope.from(mock).mokkeryContext[TestElement])
    }

    @Test
    fun testBlockRunsBeforeInstanceIsRegisteredInSuiteScope() {
        val scope = MokkerySuiteScope()
        val mock = scope.mock<RegularMethodsInterface> {
            assertEquals(emptyList(), scope.mocks)
            configurer += TestElement("suite")
        }
        assertEquals(listOf(mock), scope.mocks)
        assertEquals(TestElement("suite"), MokkeryScope.from(mock).mokkeryContext[TestElement])
    }

    @Test
    fun testFailingBlockPreventsRegistrationInSuiteScope() {
        val scope = MokkerySuiteScope()
        assertFailsWith<IllegalStateException> {
            scope.mock<RegularMethodsInterface> { error("failure") }
        }
        assertEquals(emptyList(), scope.mocks)
    }

    @Test
    fun testSpiedFunctionBlockAllowsMutatingContext() {
        val spied = spy<(Int) -> Int>({ it }) {
            configurer += TestElement("spiedFunction")
        }
        assertEquals(TestElement("spiedFunction"), MokkeryScope.from(spied).mokkeryContext[TestElement])
    }

    @Test
    fun testBlockAllowsRemovingElementAddedInSameBlock() {
        val mock = mock<RegularMethodsInterface> {
            configurer += TestElement("removed")
            configurer -= TestElement.Key
        }
        assertNull(MokkeryScope.from(mock).mokkeryContext[TestElement])
    }

    @Test
    fun testBlockAllowsRemovingElementProvidedByMokkery() {
        val mock = mock<RegularMethodsInterface> {
            configurer -= MokkeryCallHooks
        }
        assertNull(MokkeryScope.from(mock).mokkeryContext[MokkeryCallHooks])
    }

    @Test
    fun testNestedBlocksConfigureTheirOwnInstances() {
        lateinit var nested: AutoCloseable
        val mock = mock<RegularMethodsInterface> {
            configurer += TestElement("outer")
            nested = mock<AutoCloseable> {
                configurer += TestElement("nested")
            }
        }
        assertEquals(TestElement("outer"), MokkeryScope.from(mock).mokkeryContext[TestElement])
        assertEquals(TestElement("nested"), MokkeryScope.from(nested).mokkeryContext[TestElement])
    }

    @Test
    fun testNestedBlockConfiguresOuterInstanceOfDifferentType() {
        lateinit var nested: AutoCloseable
        val mock = mock<RegularMethodsInterface> {
            val outer = this
            nested = mock<AutoCloseable> {
                outer.configurer += TestElement("outerFromNested")
                configurer += TestElement("nested")
            }
        }
        assertEquals(TestElement("outerFromNested"), MokkeryScope.from(mock).mokkeryContext[TestElement])
        assertEquals(TestElement("nested"), MokkeryScope.from(nested).mokkeryContext[TestElement])
    }

    @Test
    fun testNestedBlockFailsToConfigureOuterInstanceOfSameType() {
        assertFailsWith<MokkeryRuntimeException> {
            mock<RegularMethodsInterface> {
                val outer = this
                mock<RegularMethodsInterface> {
                    outer.configurer += TestElement("outerFromNested")
                }
            }
        }
    }

    @Test
    fun testConfigurerFailsWhenUsedAfterBlock() {
        var captured: MokkeryMockConfigurer? = null
        mock<RegularMethodsInterface> { captured = configurer }
        val configurer = captured!!
        assertFailsWith<MokkeryRuntimeException> { configurer.mokkeryContext }
        assertFailsWith<MokkeryRuntimeException> { configurer.mokkeryContext = MokkeryContext.Empty }
    }

    private data class TestElement(val value: String) : MokkeryContext.Element {

        override val key = Key

        companion object Key : MokkeryContext.Key<TestElement>
    }
}
