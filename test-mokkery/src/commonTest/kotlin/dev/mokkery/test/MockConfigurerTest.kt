package dev.mokkery.test

import dev.mokkery.MockMode
import dev.mokkery.MokkeryMockScope
import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.MokkeryScope
import dev.mokkery.MokkerySuiteScope
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.returns
import dev.mokkery.configurer.MokkeryConfigurer
import dev.mokkery.configurer.MokkeryMockConfigurer
import dev.mokkery.configurer.MokkerySpyConfigurer
import dev.mokkery.configurer.mokkeryConfigurer
import dev.mokkery.configurer.minusAssign
import dev.mokkery.configurer.plusAssign
import dev.mokkery.context.MokkeryContext
import dev.mokkery.every
import dev.mokkery.interceptor.MokkeryCallHooks
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.mockMany
import dev.mokkery.mockMode
import dev.mokkery.mocks
import dev.mokkery.spy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(DelicateMokkeryApi::class)
class MockConfigurerTest {

    @Test
    fun testMockBlockAllowsMutatingContext() {
        val mock = mock<RegularMethodsInterface> {
            mokkeryConfigurer += TestElement("mock")
        }
        assertEquals(TestElement("mock"), MokkeryScope.from(mock).mokkeryContext[TestElement])
    }

    @Test
    fun testSpyBlockAllowsMutatingContext() {
        val spied = spy(listOf(1, 2, 3)) {
            mokkeryConfigurer += TestElement("spy")
        }
        assertEquals(TestElement("spy"), MokkeryScope.from(spied).mokkeryContext[TestElement])
    }

    @Test
    fun testMockManyBlockAllowsMutatingContext() {
        val mock = mockMany<RegularMethodsInterface, AutoCloseable> {
            mokkeryConfigurer += TestElement("mockMany")
        }
        assertEquals(TestElement("mockMany"), MokkeryScope.from(mock).mokkeryContext[TestElement])
    }

    @Test
    fun testFunctionMockBlockAllowsMutatingContext() {
        val mock = mock<() -> Unit> {
            mokkeryConfigurer += TestElement("function")
        }
        assertEquals(TestElement("function"), MokkeryScope.from(mock).mokkeryContext[TestElement])
    }

    @Test
    fun testClassMockBlockAllowsMutatingContext() {
        val mock = mock<AbstractClassLevel1> {
            mokkeryConfigurer += TestElement("class")
        }
        assertEquals(TestElement("class"), MokkeryScope.from(mock).mokkeryContext[TestElement])
    }

    @Test
    fun testConfigurerTypeMatchesInstanceKind() {
        var fromMock: MokkeryConfigurer? = null
        var fromSpy: MokkeryConfigurer? = null
        mock<RegularMethodsInterface> { fromMock = mokkeryConfigurer }
        spy(listOf(1, 2, 3)) { fromSpy = mokkeryConfigurer }
        assertTrue(fromMock is MokkeryMockConfigurer, "Expected mock configurer, but was $fromMock")
        assertTrue(fromSpy is MokkerySpyConfigurer, "Expected spy configurer, but was $fromSpy")
    }

    @Test
    fun testBlockRunsBeforeInstanceIsRegisteredInSuiteScope() {
        val scope = MokkerySuiteScope()
        val mock = scope.mock<RegularMethodsInterface> {
            assertEquals(emptyList(), scope.mocks)
            mokkeryConfigurer += TestElement("suite")
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
            mokkeryConfigurer += TestElement("spiedFunction")
        }
        assertEquals(TestElement("spiedFunction"), MokkeryScope.from(spied).mokkeryContext[TestElement])
    }

    @Test
    fun testBlockAllowsRemovingElementAddedInSameBlock() {
        val mock = mock<RegularMethodsInterface> {
            mokkeryConfigurer += TestElement("removed")
            mokkeryConfigurer -= TestElement.Key
        }
        assertNull(MokkeryScope.from(mock).mokkeryContext[TestElement])
    }

    @Test
    fun testBlockAllowsRemovingElementProvidedByMokkery() {
        val mock = mock<RegularMethodsInterface> {
            mokkeryConfigurer -= MokkeryCallHooks
        }
        assertNull(MokkeryScope.from(mock).mokkeryContext[MokkeryCallHooks])
    }

    @Test
    fun testBlockOverridesMockMode() {
        val mock = mock<RegularMethodsInterface>(MockMode.strict) {
            mockMode = MockMode.autoUnit
        }
        assertEquals(MockMode.autoUnit, (MokkeryScope.from(mock) as MokkeryMockScope).mockMode)
        mock.callUnit(Unit)
    }

    @Test
    fun testMockModeChangeKeepsMatchingCallsWithDefaultArgs() {
        val mock = mock<RegularMethodsInterface>(MockMode.strict) {
            mockMode = MockMode.autoUnit
        }
        every { mock.callPrimitiveWithDefaults() } returns 1
        assertEquals(1, mock.callPrimitiveWithDefaults())
    }

    @Test
    fun testNestedBlocksConfigureTheirOwnInstances() {
        lateinit var nested: AutoCloseable
        val mock = mock<RegularMethodsInterface> {
            mokkeryConfigurer += TestElement("outer")
            nested = mock<AutoCloseable> {
                mokkeryConfigurer += TestElement("nested")
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
                outer.mokkeryConfigurer += TestElement("outerFromNested")
                mokkeryConfigurer += TestElement("nested")
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
                    outer.mokkeryConfigurer += TestElement("outerFromNested")
                }
            }
        }
    }

    @Test
    fun testConfigurerFailsWhenUsedAfterBlock() {
        var captured: MokkeryMockConfigurer? = null
        mock<RegularMethodsInterface> { captured = mokkeryConfigurer }
        val configurer = captured!!
        assertFailsWith<MokkeryRuntimeException> { configurer.mokkeryContext }
        assertFailsWith<MokkeryRuntimeException> { configurer.mokkeryContext = MokkeryContext.Empty }
    }

    @Test
    fun testNestedConfigurersInvocation() {
        val mock = mock<RegularMethodsInterface> foo@{
            mockMode = MockMode.strict
            every { callComplex(any()) } returns mock {
                mockMode = MockMode.autofill
            }
        }
        assertEquals("", mock.callComplex(ComplexType("1")).id)
    }

    @Test
    fun testMockMethodsWinsWithConfigurerScopeExtensionAndAllowsSpecifyingConfigurerDirectly() {
        val mock = mock<ConflictingInterface> {
            mokkeryConfigurer.mockMode = MockMode.autoUnit
            every { mockMode } returns MockMode.original
        }
        assertEquals(MockMode.original, mock.mockMode)
        mock.callUnit()
    }

    private data class TestElement(val value: String) : MokkeryContext.Element {

        override val key = Key

        companion object Key : MokkeryContext.Key<TestElement>
    }
}

private interface ConflictingInterface {

    val mockMode: MockMode
    fun callUnit()
}
