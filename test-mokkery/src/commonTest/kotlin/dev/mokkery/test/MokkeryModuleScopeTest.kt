package dev.mokkery.test

import dev.mokkery.MockMode
import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.MokkeryScope
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.annotations.MokkeryModuleInit
import dev.mokkery.configurer.MokkeryConfigurer
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.defaultMockMode
import dev.mokkery.internal.defaultVerifyMode
import dev.mokkery.internal.moduleName
import dev.mokkery.internal.mokkeryInternals
import dev.mokkery.verify.VerifyMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertSame

private var moduleContextFromInit: MokkeryContext? = null
private var configuredElementFromInit: TestElement? = null
private var initWithoutParamsCalled = false
private var elementBeforeConfiguration: TestElement? = null
private var scopeFromInit: MokkeryScope? = null

@MokkeryModuleInit
internal fun initModuleBeforeConfiguration(scope: MokkeryScope) {
    elementBeforeConfiguration = scope.mokkeryContext[TestElement]
    scopeFromInit = scope
}

@MokkeryModuleInit
internal fun configureModule(configurer: MokkeryConfigurer) {
    @OptIn(DelicateMokkeryApi::class)
    configurer.mokkeryContext += TestElement
}

@MokkeryModuleInit
internal fun initModule(scope: MokkeryScope) {
    moduleContextFromInit = scope.mokkeryContext
    configuredElementFromInit = scope.mokkeryContext[TestElement]
}

@MokkeryModuleInit
internal fun initModuleWithoutParams() {
    initWithoutParamsCalled = true
}

private object TestElement : MokkeryContext.Element, MokkeryContext.Key<TestElement> {

    override val key: MokkeryContext.Key<*> get() = this
}

@OptIn(InternalMokkeryApi::class)
class MokkeryModuleScopeTest {

    @Test
    fun test() {
        val module = assertNotNull(MokkeryScope.module)
        val internals = module.mokkeryInternals
        assertEquals("dev.mokkery:test-mokkery_test", internals.moduleName)
        assertEquals(MockMode.strict, internals.defaultMockMode)
        assertEquals(VerifyMode.soft, internals.defaultVerifyMode)
        assertEquals("MokkeryScope(mokkeryContext=${module.mokkeryContext})", module.toString())
    }

    @Test
    fun testInitWithScopeParamReceivesConfiguredModuleScope() {
        val module = MokkeryScope.module
        assertSame(module.mokkeryContext, moduleContextFromInit)
        assertEquals(TestElement, configuredElementFromInit)
    }

    @Test
    fun testInitializersRunInDeclarationOrder() {
        MokkeryScope.module
        assertEquals(null, elementBeforeConfiguration)
        assertEquals(TestElement, configuredElementFromInit)
    }

    @Test
    fun testInitWithoutParamsIsCalled() {
        MokkeryScope.module
        assertEquals(true, initWithoutParamsCalled)
    }

    @Test
    fun testInitWithConfigurerParamCanChangeModuleScope() {
        assertEquals(TestElement, MokkeryScope.module.mokkeryContext[TestElement])
    }

    @Test
    fun testScopeRetainedFromInitIsModuleScope() {
        val module = MokkeryScope.module
        val retained = assertNotNull(scopeFromInit)
        assertSame(module, retained)
        assertEquals(TestElement, retained.mokkeryContext[TestElement])
    }

    @OptIn(DelicateMokkeryApi::class)
    @Test
    fun testModuleScopeCannotBeModifiedAfterInit() {
        val configurer = MokkeryScope.module as MokkeryConfigurer
        val error = assertFailsWith<MokkeryRuntimeException> { configurer.mokkeryContext += TestElement }
        assertEquals("Module scope can only be modified in `@MokkeryModuleInit` functions!", error.message)
    }

    @Test
    fun testModuleInMain() {
        val module = assertNotNull(mainMokkeryModule)
        val internals = module.mokkeryInternals
        assertEquals("dev.mokkery:test-mokkery", internals.moduleName)
        assertEquals(MockMode.strict, internals.defaultMockMode)
        assertEquals(VerifyMode.soft, internals.defaultVerifyMode)
        assertEquals("MokkeryScope(mokkeryContext=${module.mokkeryContext})", module.toString())
    }
}
