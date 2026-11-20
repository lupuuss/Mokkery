package dev.mokkery.test

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.MokkeryScope
import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.answering.returns
import dev.mokkery.answering.returnsArgAt
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.internal.mokkeryInternals
import dev.mokkery.internal.resetMocksCounter
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.any
import dev.mokkery.matcher.logical.not
import dev.mokkery.matcher.logical.or
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.templating.ctx
import dev.mokkery.templating.ext
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(InternalMokkeryApi::class)
class TemplatingTest {

    @BeforeTest
    fun before() {
        MokkeryScope.global
            .mokkeryInternals
            .resetMocksCounter()
    }

    @Test
    fun testEmptyEveryCall() {
        assertFailsWithSingleEveryCallButNoCallsAtAll {
            every { }
        }
    }

    @Test
    fun testEveryWithNonMockCall() {
        assertFailsWithSingleEveryCallButNoCallsAtAll {
            val list = listOf<Int>()
            every { list.size }
        }
    }

    @Test
    fun testNonMockReceiverEvaluatedOnce() {
        var evaluations = 0
        fun provide(): List<Int> {
            evaluations++
            return listOf(1)
        }
        verify { provide().size }
        assertEquals(1, evaluations)
    }

    @Test
    fun testLambdaNonMockReceiverEvaluatedOnce() {
        var evaluations = 0
        verify { ({ evaluations++ })() }
        assertEquals(1, evaluations)
    }

    @Test
    fun testEveryWithMoreThanOneCall() {
        MokkeryScope.global.mokkeryInternals.resetMocksCounter()
        assertMokkeryError(
            """
                Each 'every' block requires exactly one call to a mock, but there are more calls than expected:
                1. RegularMethodsInterface(1).callPrimitive(input = any())
                2. RegularMethodsInterface(1).callComplex(input = any())
                
            """.trimIndent()
        ) {
            val mock = mock<RegularMethodsInterface>()
            every {
                mock.callPrimitive(any())
                mock.callComplex(any())
            }
        }
    }

    @Test
    fun testFinalMockCallInEveryBlock() {
        val mock = mock<AbstractClassLevel1>()
        assertFailsWithMockFinalCall("AbstractClassLevel1(1)", "finalMethod") {
            every { mock.finalMethod() }
        }
    }

    @Test
    fun testFinalMockCallInVerifyBlock() {
        val mock = mock<AbstractClassLevel1>()
        assertFailsWithMockFinalCall("AbstractClassLevel1(1)", "finalMethod") {
            verify { mock.finalMethod() }
        }
    }

    @Test
    fun testFailsWithResultAccessErrorWhenMockCallIsPassedToFinalMemberCall() {
        val mock = mock<RegularMethodsInterface>()
        val holder = FinalMemberHolder()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            verify { holder.finalCall(mock.callPrimitive(1)) }
        }
    }

    @Test
    fun testFailsWithFinalCallErrorWhenFinalMockCallIsPassedToMockCall() {
        val final = mock<AbstractClassLevel1>()
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithMockFinalCall("AbstractClassLevel1(1)", "finalMethod") {
            verify { mock.callPrimitive(final.finalMethod().length) }
        }
    }

    @Test
    fun testFinalMockCallInEveryWithFunctionReference() {
        val mock = mock<AbstractClassLevel1>()
        assertFailsWithMockFinalCall("AbstractClassLevel1(1)", "finalMethod") {
            every(mock::finalMethod)
        }
    }

    @Test
    fun testInlineMockCallInEveryBlock() {
        val mock = mock<AbstractClassLevel1>()
        assertFailsWithMockFinalCall("AbstractClassLevel1(1)", "inlineMethod") {
            every { mock.inlineMethod() }
        }
    }

    @Test
    fun testInlineMockCallInVerifyBlock() {
        val mock = mock<AbstractClassLevel1>()
        assertFailsWithMockFinalCall("AbstractClassLevel1(1)", "inlineMethod") {
            verify { mock.inlineMethod() }
        }
    }

    @Test
    fun testInlinePropertyMockCallInEveryBlock() {
        val mock = mock<AbstractClassLevel1>()
        assertFailsWithMockFinalCall("AbstractClassLevel1(1)", "get inlineProperty") {
            every { mock.inlineProperty }
        }
    }

    @Test
    fun testInlinePropertyMockCallInVerifyBlock() {
        val mock = mock<AbstractClassLevel1>()
        assertFailsWithMockFinalCall("AbstractClassLevel1(1)", "set inlineProperty") {
            verify { mock.inlineProperty = "" }
        }
    }

    @Test
    fun testToStringMockCallInEveryBlock() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithFunctionCannotBeMocked("toString") {
            every { mock.toString() }
        }
    }

    @Test
    fun testToStringMockCallInVerifyBlock() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithFunctionCannotBeMocked("toString") {
            verify { mock.toString() }
        }
    }

    @Test
    fun testHashCodeMockCallInEveryBlock() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithFunctionCannotBeMocked("hashCode") {
            every { mock.hashCode() }
        }
    }

    @Test
    fun testEqualsMockCallInEveryBlock() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithFunctionCannotBeMocked("equals") {
            every { mock.equals("other") }
        }
    }

    @Test
    fun testUnwrapping() {
        val mock = mock<RegularMethodsInterface>()
        val mocks = listOf(mock)
        every { mocks[0].callPrimitive(or(1, 2, 3)) } returnsArgAt 0
        assertEquals(1, mocks[0].callPrimitive(1))
        assertEquals(2, mocks[0].callPrimitive(2))
        assertEquals(3, mocks[0].callPrimitive(3))
        assertFailsWith<MokkeryRuntimeException> { mocks[0].callPrimitive(4) }
        verify {
            mocks[0].callPrimitive(1)
            mocks[0].callPrimitive(2)
            mocks[0].callPrimitive(3)
        }
    }

    @Test
    fun testFailsAccessingMockCallResultInVariable() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            verify {
                val variable = mock.callPrimitive(0)
            }
        }
    }

    @Test
    fun testFailsAccessingMockCallResultReturnedFromExt() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitiveExtension") {
            verify { mock.ext { 1.callPrimitiveExtension() }.toString() }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultInWhen() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            verify {
                val variable = when (true) {
                    true -> mock.callPrimitive(1)
                    else -> Unit
                }
            }
        }
    }


    @Test
    fun testUnwrapsNonMockCallResultInStringTemplate() {
        val mock = mock<RegularMethodsInterface> { every { callOverloaded(any<String>()) } returns "" }
        val list = listOf(1, 2, 3)
        mock.callOverloaded("size = 3")
        verify { mock.callOverloaded("size = ${list.size}") }
    }

    @Test
    fun testUnwrapsNonMockCallResultInVararg() {
        val mock = mock<CollectionsInterface> { every { callWithInts(any()) } returns 0 }
        val list = listOf(1, 2, 3)
        mock.callWithInts(3)
        verify { mock.callWithInts(list.size) }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultInStringTemplate() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            verify {
                mock.callOverloaded("value = ${mock.callPrimitive(1)}")
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultInVararg() {
        val mock = mock<CollectionsInterface>()
        assertFailsWithResultAccessError("CollectionsInterface(1)", "callWithInts") {
            verify {
                mock.callWithInts(mock.callWithInts(1))
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultInSpread() {
        val mock = mock<CollectionsInterface>()
        assertFailsWithResultAccessError("CollectionsInterface(1)", "callWithInts") {
            verify {
                mock.callWithInts(*intArrayOf(mock.callWithInts(1)))
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultInMatcherBuilderArgument() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            verify {
                val matcher = not(mock.callPrimitive(1))
                mock.callPrimitive(matcher)
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultInScopeFunctionInMatcherBuilderArgument() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            verify {
                val matcher = not(mock.let { it.callPrimitive(1) })
                mock.callPrimitive(matcher)
            }
        }
    }

    @Test
    fun testAllowsMockCallsInMatcherLambdas() {
        val other = mock<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }
        val mock = mock<RegularMethodsInterface>()
        every {
            mock.callPrimitive(matches(toString = { "${other.callPrimitive(2)}" }) { other.callPrimitive(it) == 2 })
        } returns 1
        assertEquals(1, mock.callPrimitive(2))
        verify { mock.callPrimitive(matches(toString = { "${other.callPrimitive(2)}" }) { other.callPrimitive(it) == 2 }) }
    }

    @Test
    fun testAllowsMockCallsInMatcherObjects() {
        val other = mock<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }
        val mock = mock<RegularMethodsInterface>()
        every {
            mock.callPrimitive(matches(ArgMatcher<Int> { arg -> other.callPrimitive(arg) == 2 }))
        } returns 1
        assertEquals(1, mock.callPrimitive(2))
        verify {
            val matcher = matches(object : ArgMatcher<Int> {
                override fun matches(arg: Int) = other.callPrimitive(arg) == 2
            })
            mock.callPrimitive(matcher)
        }
    }

    @Test
    fun testAllowsMockCallsInMatcherLambdasOfMatcherVariable() {
        val other = mock<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }
        val mock = mock<RegularMethodsInterface>()
        every {
            val matcher = matches<Int>(toString = { "${other.callPrimitive(2)}" }) { other.callPrimitive(it) == 2 }
            mock.callPrimitive(matcher)
        } returns 1
        assertEquals(1, mock.callPrimitive(2))
        verify {
            val matcher = not(matches<Int>(toString = { "${other.callPrimitive(2)}" }) { other.callPrimitive(it) == 3 })
            mock.callPrimitive(matcher)
        }
    }

    @Test
    fun testAllowsMockCallsInMatcherLambdasAndObjectsDeclaredInBlock() {
        val item = mock<ComplexType> { every { id } returns "1" }
        val mock = mock<RegularMethodsInterface>()
        every {
            val predicate: (ComplexType) -> Boolean = { it.id == "1" }
            mock.callComplex(matches(toString = { "id 1" }, predicate = predicate))
        } returns item
        assertEquals(item, mock.callComplex(item))
        verify {
            val matcher = object : ArgMatcher<ComplexType> {
                override fun matches(arg: ComplexType) = arg.id == "1"
            }
            mock.callComplex(matches(matcher))
        }
    }

    @Test
    fun testTemplatesMockCallsInNonInlineLambdasCalledInBlock() {
        val mock = mock<RegularMethodsInterface> { every { callPrimitive(any()) } returns 1 }
        mock.callPrimitive(1)
        verify { eager { mock.callPrimitive(1) } }
        assertFailsWith<AssertionError> { verify { eager { mock.callPrimitive(2) } } }
    }

    @Test
    fun testFailsWhenMockIsCalledInNonInlineLambdaCalledInArgument() {
        val other = mock<RegularMethodsInterface>()
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            every { mock.callPrimitive(eager { other.callPrimitive(1) }) }
        }
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            verify { mock.callPrimitive(not(eagerInline { other.callPrimitive(1) })) }
        }
    }

    @Test
    fun testTemplatesMockCallsInLambdasPassedToOpenMembers() {
        val mock = mock<RegularMethodsInterface> { every { callPrimitive(any()) } returns 1 }
        mock.callPrimitive(1)
        verify { Runner().openRun { mock.callPrimitive(1) } }
        assertFailsWith<AssertionError> { verify { Runner().openRun { mock.callPrimitive(2) } } }
    }

    @Test
    fun testTemplatesMockCallsInLambdasPassedToFinalMembersOfOpenClasses() {
        val mock = mock<RegularMethodsInterface> { every { callPrimitive(any()) } returns 1 }
        mock.callPrimitive(1)
        verify { Runner().finalRun { mock.callPrimitive(1) } }
        assertFailsWith<AssertionError> { verify { Runner().finalRun { mock.callPrimitive(2) } } }
    }

    @Test
    fun testFinalMemberCallOnResultOfOpenMemberCall() {
        val mock = mock<RegularMethodsInterface> { every { callPrimitive(any()) } returns 1 }
        mock.callPrimitive(1)
        verify {
            RunnerProvider().runner().finalRun { 1 }
            mock.callPrimitive(1)
        }
    }

    private open class Runner {
        open fun <T> openRun(block: () -> T): T = block()
        fun <T> finalRun(block: () -> T): T = block()
    }

    private open class RunnerProvider {
        open fun runner(): Runner = Runner()
    }

    private fun <T> eager(block: () -> T): T = block()

    @Suppress("NOTHING_TO_INLINE")
    private inline fun <T> eagerInline(noinline block: () -> T): T = block()

    @Test
    fun testFailsWhenAccessingMockCallResultOfWhenWithAllBranchesReturningIt() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            verify {
                val variable = when (true) {
                    true -> mock.callPrimitive(1)
                    else -> mock.callPrimitive(2)
                }
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultOfTryWithAllBranchesReturningIt() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            verify {
                val variable = try {
                    mock.callPrimitive(1)
                } catch (e: Throwable) {
                    mock.callPrimitive(2)
                }
            }
        }
    }

    @Test
    fun testAllowsMockCallInIfWithoutElseBranch() {
        val mock = mock<RegularMethodsInterface> { every { callPrimitive(any()) } returns 0 }
        mock.callPrimitive(1)
        verify {
            if (isTrue) mock.callPrimitive(1)
        }
    }

    @Test
    fun testAllowsMockCallInWhenWithThrowingBranch() {
        val mock = mock<RegularMethodsInterface> { every { callPrimitive(any()) } returns 0 }
        mock.callPrimitive(1)
        verify {
            when (isTrue) {
                true -> mock.callPrimitive(1)
                else -> error("Unexpected!")
            }
        }
    }

    @Test
    fun testAllowsMockCallInTryWithThrowingCatch() {
        val mock = mock<RegularMethodsInterface> { every { callPrimitive(any()) } returns 0 }
        mock.callPrimitive(1)
        verify {
            try {
                mock.callPrimitive(1)
            } catch (e: Throwable) {
                throw e
            }
        }
    }

    @Test
    fun testAllowsMockCallInWhenWithReturningBranch() {
        val mock = mock<RegularMethodsInterface> { every { callPrimitive(any()) } returns 0 }
        mock.callPrimitive(1)
        verify {
            when {
                isTrue -> mock.callPrimitive(1)
                else -> return@verify
            }
        }
    }

    @Test
    fun testAllowsSafeCallOnMockInEveryBlock() {
        nullableMock = mock<RegularMethodsInterface>()
        every { nullableMock?.callPrimitive(any()) } returns 1
        assertEquals(1, nullableMock?.callPrimitive(0))
    }

    @Test
    fun testAllowsSafeCallOnMockInVerifyBlock() {
        val mock = mock<RegularMethodsInterface> { every { callPrimitive(any()) } returns 0 }
        nullableMock = mock
        mock.callPrimitive(1)
        verify { nullableMock?.callPrimitive(1) }
    }

    @Test
    fun testAllowsSafeCallOnMockInSuspendTemplatingBlocks() = runTest {
        val mock = mock<SuspendMethodsInterface> { everySuspend { callPrimitive(any()) } returns 0 }
        nullableSuspendMock = mock
        mock.callPrimitive(1)
        verifySuspend { nullableSuspendMock?.callPrimitive(1) }
    }

    @Suppress("USELESS_IS_CHECK")
    @Test
    fun testFailsWhenAccessingMockCallResultInIsCheck() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callComplex") {
            verify {
                if (mock.callComplex(ComplexType) is ComplexType) Unit
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultInLocalClassField() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            verify {
                class Local {
                    val value = mock.callPrimitive(1)
                }
                Local()
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultInThrow() {
        val mock = mock<ThrowableProvider>()
        assertFailsWithResultAccessError("ThrowableProvider(1)", "provide") {
            verify {
                throw mock.provide()
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultInArrayLiteral() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            verify {
                val ints = intArrayOf(mock.callPrimitive(1))
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultInNestedFunction() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            verify {
                fun nested() = mock.callPrimitive(1)
                nested()
            }
        }
    }


    @Test
    fun testFailsWhenWrappingMockCallInScopeFunction() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            every {
                1.let { mock.callPrimitive(it) }
            }
        }
    }

    @Test
    fun testFailsWhenWrappingMockCallInRunScopeFunction() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            every {
                run { mock.callPrimitive(1) }
            }
        }
    }

    @Test
    fun testAllowsRunScopeFunctionInEveryBlock() {
        val mock = mock<RegularMethodsInterface> { every { callPrimitive(run { 1 }) } returns 2 }
        assertEquals(2, mock.callPrimitive(1))
    }

    @Test
    fun testAllowsRunScopeFunctionInVerifyBlock() {
        val mock = mock<RegularMethodsInterface> { every { callPrimitive(any()) } returns 0 }
        mock.callPrimitive(1)
        verify { run { mock.callPrimitive(1) } }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultInAnotherCall() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            verify {
                listOf(mock.callPrimitive(1))
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultInCondition() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            verify {
                if (mock.callPrimitive(1) == 1) return@verify
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultAsIfCondition() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callBoolean") {
            verify {
                if (mock.callBoolean(false)) return@verify
            }
        }
    }

    @Test
    fun testFailsWhenAccessingMockCallResultAsLoopCondition() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callBoolean") {
            verify {
                while (mock.callBoolean(false)) return@verify
            }
        }
    }

    @Test
    fun testFailsWhenPassingMockCallResultToOtherMock() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            verify {
                mock.callPrimitive(mock.callPrimitive(1))
            }
        }
    }


    @Test
    fun testFailsWhenPassingNestedMockCallResultToOtherMethodCall() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            val list = listOf<Int>()
            verify {
                mock.callPrimitive(list.getOrElse(mock.callPrimitive(1)) { 0 })
            }
        }
    }

    @Test
    fun testFailsWhenPassingMockCallResultToMethodCall() {
        val mock = mock<RegularMethodsInterface>()
        assertFailsWithResultAccessError("RegularMethodsInterface(1)", "callPrimitive") {
            val list = listOf<Int>()
            verify {
                list[mock.callPrimitive(1)]
            }
        }
    }

    @Test
    fun testFailsWhenFunctionReferenceUsedWithNonMockOverridableType() {
        assertMokkeryError(
            """
                Call to `get` was expected to be performed on a mock of List type, but the receiver was not a mock - it was an instance of EmptyList type => []
            """.trimIndent()
        ) {
            val list = listOf<Int>()
            every(list::get)
        }
    }

    @Test
    fun testFailsWhenMatcherUsedWithNonMockOverridableType() {
        assertMokkeryError(
            """
                Call to `get` was expected to be performed on a mock of List type, but the receiver was not a mock - it was an instance of EmptyList type => []
            """.trimIndent()
        ) {
            val list = listOf<Int>()
            verify {
                list[any()]
            }
        }
    }

    @Test
    fun testFailsWhenContextOfMatcherUsedWithNonMockOverridableType() {
        assertMokkeryError(
            """
                Call to `get` was expected to be performed on a mock of List type, but the receiver was not a mock - it was an instance of EmptyList type => []
            """.trimIndent()
        ) {
            val list = listOf<Int>()
            verify {
                ctx(any<Int>()) { list[contextOf<Int>()] }
            }
        }
    }

    @Test
    fun testFailsWhenMatcherUsedWithNonMockOverridableAnonymousType() {
        assertMokkeryError(
            """
                Call to `compareTo` was expected to be performed on a mock of Comparable type, but the receiver was not a mock - it was an instance of anonymous type => comparable-to-string
            """.trimIndent()
        ) {
            val comparable: Comparable<Int> = object : Comparable<Int> {
                override fun compareTo(other: Int): Int = other

                override fun toString(): String = "comparable-to-string"
            }
            verify {
                comparable.compareTo(any())
            }
        }
    }

    @Test
    fun testSelf() {
        val mock = mock<SelfType>()
        every { mock.callWithSelf(mock) } returns mock
        every { mock.callWithListSelf(listOf(mock)) } returns listOf(mock)
        assertEquals(mock, mock.callWithSelf(mock))
        assertEquals(listOf(mock), mock.callWithListSelf(listOf(mock)))
    }

    @Test
    fun testStarProjectedMockWithVarargAndOmittedDefault() {
        val typed = mock<GenericFunctionsInterface<String>>()
        val mock: GenericFunctionsInterface<*> = typed
        every { mock.callVararg(any(), any()) } returns 1
        every { mock.callWithDefault(any()) } returns 2
        assertEquals(1, typed.callVararg("a", "b"))
        assertEquals(2, typed.callWithDefault(0))
        assertFailsWith<MokkeryRuntimeException> { typed.callWithDefault(0, listOf("a")) }
    }

    @Test
    fun testArgMatchersScopeAccess() {
        val mock = mock<RegularMethodsInterface>()
        verify(not) {
            mock.callPrimitive(1)
            println(this)
            mock.callPrimitive(any())
            println(this)
            mock.callPrimitive(2)
            println(this)
        }
    }

    // prevents the compiler from removing branches of a constant condition
    private val isTrue: Boolean get() = true

    // mocks kept as nullable properties, so that safe calls are not optimized out by the compiler
    private var nullableMock: RegularMethodsInterface? = null
    private var nullableSuspendMock: SuspendMethodsInterface? = null

    private fun assertFailsWithResultAccessError(receiver: String, functionName: String, block: () -> Unit) {
        assertMokkeryError(
            expectedMessage = """
                The result of calling `$functionName` on $receiver must not be accessed inside `every` or `verify`.
                
                If you're trying to mock a member function with an extension receiver or context parameters, use `dev.mokkery.templating.ext` or `dev.mokkery.templating.ctx` instead of Kotlin scope functions (e.g. `let`, `run`). 
                Otherwise, using scope functions here is not supported.
            """.trimIndent(),
            block = block
        )
    }

    private fun assertFailsWithFunctionCannotBeMocked(functionName: String, block: () -> Unit) {
        assertMokkeryError("Function `$functionName` cannot be mocked!") {
            block()
        }
    }

    private fun assertFailsWithMockFinalCall(receiver: String, functionName: String, block: () -> Unit) {
        assertMokkeryError(
            """
                `$functionName` is final and cannot be mocked on $receiver.
                Only non-final member functions can be intercepted inside `every` or `verify`.
            """.trimIndent()
        ) {
            block()
        }
    }

    private fun assertFailsWithSingleEveryCallButNoCallsAtAll(block: () -> Unit) {
        assertMokkeryError(
            expectedMessage = """
                Each 'every' block requires exactly one call to a mock, but there are no calls to any mock!
                
                Possible reasons:
                * You are calling an object that is not a mock.
                * You are calling a mock, but the member function is final.
                * You are calling a mock, but it's an extension function instead of a member function.
                
            """.trimIndent(),
            block = block
        )
    }

    private interface ThrowableProvider {

        fun provide(): Throwable
    }

    private interface SelfType {

        fun callWithSelf(self: SelfType): SelfType

        fun callWithListSelf(self: List<SelfType>): List<SelfType>
    }

    open class FinalMemberHolder {
        fun finalCall(value: Int) = value
    }
}
