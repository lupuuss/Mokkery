package dev.mokkery.test.defaults

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.returns
import dev.mokkery.answering.returnsArgAt
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.templating.ext
import dev.mokkery.test.FunctionDefaultsInterface
import dev.mokkery.test.GenericDefaultsInterface
import dev.mokkery.test.SelfReferencingDefaultsInterface
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DefaultsTest {

    private val mock = mock<FunctionDefaultsInterface>()

    @Test
    fun testWithComputedDefaults() {
        every { mock.call(5) } returnsArgAt 1
        assertEquals("name(5)", mock.call(5))
        assertEquals("name(5)", mock.call(5, "name(5)"))
        assertEquals("name(5)", mock.call(5, "name(5)", "name5@mail.com"))
        assertFailsWith<MokkeryRuntimeException> { mock.call(4)  }
        assertFailsWith<MokkeryRuntimeException> { mock.call(5, "not-name") }
        assertFailsWith<MokkeryRuntimeException> { mock.call(5, mail = "not-mail") }
        verify { mock.call(5) }
    }

    @Test
    fun testWithAllValues() {
        every { mock.call(5, "name", "mail") } returnsArgAt 1
        assertEquals("name", mock.call(5, "name", "mail"))
        assertFailsWith<MokkeryRuntimeException> { mock.call(4, "name", "mail") }
        verify { mock.call(5, "name", "mail") }
    }

    @Test
    fun testWithMultipleExplicitArgsAndOmittedDefault() {
        every { mock.call(5, "name") } returnsArgAt 1
        assertEquals("name", mock.call(5, "name"))
        assertEquals("name", mock.call(5, "name", "name5@mail.com"))
        assertFailsWith<MokkeryRuntimeException> { mock.call(5, "name", "not-mail") }
        verify { mock.call(5, "name") }
    }

    @Test
    fun testWithMultipleExplicitArgsAndOmittedDefaultForSuspend() = runTest {
        everySuspend { mock.callSuspend(5, "name") } returnsArgAt 1
        assertEquals("name", mock.callSuspend(5, "name"))
        assertFailsWith<MokkeryRuntimeException> { mock.callSuspend(5, "name", "not-mail") }
        verifySuspend { mock.callSuspend(5, "name") }
    }

    @Test
    fun testWithMultipleExplicitArgsAndOmittedDefaultForExtension() {
        every { mock.ext { 5.callExtension("name") } } returnsArgAt 1
        assertEquals("name", mock.run { 5.callExtension("name") })
        assertFailsWith<MokkeryRuntimeException> { mock.run { 5.callExtension("name", "not-mail") } }
        verify { mock.ext { 5.callExtension("name") } }
    }

    @Test
    fun testWithComputedDefaultsForSuspend() = runTest {
        everySuspend { mock.callSuspend(5) } returnsArgAt 2
        assertEquals("name5@mail.com", mock.callSuspend(5))
        assertEquals("name5@mail.com", mock.callSuspend(5, "name(5)"))
        assertFailsWith<MokkeryRuntimeException> { mock.callSuspend(5, "not-name") }
        verifySuspend { mock.callSuspend(5) }
    }

    @Test
    fun testWithComputedDefaultsForExtension() {
        every { mock.ext { 5.callExtension() } } returnsArgAt 2
        assertEquals("name5@mail.com", mock.run { 5.callExtension() })
        assertEquals("name5@mail.com", mock.run { 5.callExtension("name(5)") })
        assertFailsWith<MokkeryRuntimeException> { mock.run { 5.callExtension("not-name") } }
        assertFailsWith<MokkeryRuntimeException> { mock.run { 4.callExtension() } }
        verify { mock.ext { 5.callExtension() } }
    }

    @Test
    fun testWithComputedDefaultsForGenericType() {
        val generic = mock<GenericDefaultsInterface<Int>>()
        every { generic.call(5) } returnsArgAt 2
        assertEquals("name(5)", generic.run { call(5) })
        assertFailsWith<MokkeryRuntimeException> { generic.call(5, 1) }
        verify { generic.call(5) }
    }

    @Test
    fun testFailsWithClearErrorWhenDefaultExpressionUsesProperty() {
        val selfReferencing = mock<SelfReferencingDefaultsInterface>()
        every { selfReferencing.defaultName } returns "cfg"
        every { selfReferencing.callWithPropertyDefault(5) } returns "ok"
        val exception = assertFailsWith<MokkeryRuntimeException> { selfReferencing.callWithPropertyDefault(5) }
        assertEquals(
            expectedMessage(selfReferencing, "callWithPropertyDefault(i = 5, name = default())", listOf("name"), "defaultName"),
            exception.message
        )
    }

    @Test
    fun testFailsWithClearErrorWhenDefaultExpressionUsesFunction() {
        val selfReferencing = mock<SelfReferencingDefaultsInterface>()
        every { selfReferencing.defaultMail(5) } returns "mail"
        every { selfReferencing.callWithFunctionDefault(5) } returns "ok"
        val exception = assertFailsWith<MokkeryRuntimeException> { selfReferencing.callWithFunctionDefault(5) }
        assertEquals(
            expectedMessage(selfReferencing, "callWithFunctionDefault(i = 5, mail = default())", listOf("mail"), "defaultMail"),
            exception.message
        )
    }

    @Test
    fun testFailsWithClearErrorWhenDefaultExpressionUsesOverloadOfTheSameArity() {
        val selfReferencing = mock<SelfReferencingDefaultsInterface>()
        every { selfReferencing.overloaded(1, 2) } returns "n"
        every { selfReferencing.overloaded("s") } returns "ok"
        val exception = assertFailsWith<MokkeryRuntimeException> { selfReferencing.overloaded("s") }
        assertEquals(
            expectedMessage(selfReferencing, "overloaded(x = \"s\", y = default())", listOf("y"), "overloaded"),
            exception.message
        )
    }

    @Test
    fun testFailsWithClearErrorWhenDefaultExpressionUsesOverloadWithTheSameParameterNames() {
        val selfReferencing = mock<SelfReferencingDefaultsInterface>()
        every { selfReferencing.sameParameterNames(1, 2) } returns "n"
        every { selfReferencing.sameParameterNames("s") } returns "ok"
        val exception = assertFailsWith<MokkeryRuntimeException> { selfReferencing.sameParameterNames("s") }
        assertEquals(
            expectedMessage(selfReferencing, "sameParameterNames(a = \"s\", b = default())", listOf("b"), "sameParameterNames"),
            exception.message
        )
    }

    @Test
    fun testSelfReferencingDefaultWorksWhenPassedExplicitly() {
        val selfReferencing = mock<SelfReferencingDefaultsInterface>()
        every { selfReferencing.defaultMail(5) } returns "mail"
        every { selfReferencing.callWithFunctionDefault(5, "mail") } returns "ok"
        assertEquals("ok", selfReferencing.callWithFunctionDefault(5))
    }

    @Test
    fun testSafeDefaultIsResolvedWhenProblematicDefaultIsPassedExplicitly() {
        val selfReferencing = mock<SelfReferencingDefaultsInterface>()
        every { selfReferencing.defaultName } returns "cfg"
        every { selfReferencing.mixedDefaults(5, "cfg") } returns "ok"
        assertEquals("ok", selfReferencing.mixedDefaults(5))
        assertEquals("ok", selfReferencing.mixedDefaults(5, "cfg", "tag"))
        assertFailsWith<MokkeryRuntimeException> { selfReferencing.mixedDefaults(5, "cfg", "other") }
        verify { selfReferencing.mixedDefaults(5, "cfg") }
    }

    @Test
    fun testFailsWhenProblematicDefaultIsOmittedEvenThoughOtherDefaultIsSafe() {
        val selfReferencing = mock<SelfReferencingDefaultsInterface>()
        every { selfReferencing.defaultName } returns "cfg"
        every { selfReferencing.mixedDefaults(5) } returns "ok"
        val exception = assertFailsWith<MokkeryRuntimeException> { selfReferencing.mixedDefaults(5) }
        assertEquals(
            expectedMessage(
                mock = selfReferencing,
                call = "mixedDefaults(i = 5, name = default(), tag = default())",
                omitted = listOf("name", "tag"),
                usedMember = "defaultName"
            ),
            exception.message
        )
    }

    @Test
    fun testSelfReferencingPropertyDefaultWorksWhenPassedExplicitly() {
        val selfReferencing = mock<SelfReferencingDefaultsInterface>()
        every { selfReferencing.defaultName } returns "cfg"
        every { selfReferencing.callWithPropertyDefault(5, "cfg") } returns "ok"
        assertEquals("ok", selfReferencing.callWithPropertyDefault(5))
        verify { selfReferencing.callWithPropertyDefault(5, "cfg") }
    }

    @Test
    fun testSelfReferencingOverloadDefaultWorksWhenPassedExplicitly() {
        val selfReferencing = mock<SelfReferencingDefaultsInterface>()
        every { selfReferencing.overloaded(1, 2) } returns "n"
        every { selfReferencing.overloaded("s", "n") } returns "ok"
        assertEquals("ok", selfReferencing.overloaded("s"))
        verify { selfReferencing.overloaded("s", "n") }
    }

    @Test
    fun testSelfReferencingOverloadWithTheSameParameterNamesWorksWhenPassedExplicitly() {
        val selfReferencing = mock<SelfReferencingDefaultsInterface>()
        every { selfReferencing.sameParameterNames(1, 2) } returns "n"
        every { selfReferencing.sameParameterNames("s", "n") } returns "ok"
        assertEquals("ok", selfReferencing.sameParameterNames("s"))
        verify { selfReferencing.sameParameterNames("s", "n") }
    }

    @Test
    fun testDoesNotAffectIdsOfMocksCreatedAfterExtraction() {
        val first = mock<FunctionDefaultsInterface>()
        every { first.call(5) } returnsArgAt 1
        first.call(5)
        val second = mock<FunctionDefaultsInterface>()
        assertEquals(first.instanceIdNumber() + 1, second.instanceIdNumber())
    }

    private fun expectedMessage(mock: Any, call: String, omitted: List<String>, usedMember: String): String =
        "Call template `${mock.toString().substringAfterLast(".")}.$call` relies on the default value of" +
                " ${omitted.joinToString { "`$it`" }}," +
                " but one of those defaults is computed from `$usedMember` of the same mocked instance," +
                " which Mokkery cannot resolve." +
                " Pass that argument explicitly in the `every`/`verify` block that registered this template."

    private fun Any.instanceIdNumber(): Long = toString()
        .substringAfterLast("(")
        .removeSuffix(")")
        .toLong()
}
