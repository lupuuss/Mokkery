package dev.mokkery.test

import com.tschuchort.compiletesting.KotlinCompilation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MatchersUsageInMatcherBuilderTest {

    @Test
    fun testForbidsOperatorOnMatcher() {
        tests(
            "::class" to "::class",
            " == 1" to "==",
            " === 1" to "===",
        ) { (expression, operator) ->
            compileJvm(
                """
            import dev.mokkery.matcher.MokkeryMatcherScope
            import dev.mokkery.matcher.any
        
            fun <T> MokkeryMatcherScope.matcher(): T {
                any<Int>()$expression
                return any()
            }
        """.trimIndent()
            ).assertSingleError("Operators cannot be used with matchers, but '$operator' operator is used")
        }
    }

    @Test
    fun testForbidsOperatorOnMatcherVariable() {
        tests(
            "::class" to "::class",
            " == 1" to "==",
            " === 1" to "===",
        ) { (expression, operator) ->
            compileJvm(
                """
            import dev.mokkery.matcher.MokkeryMatcherScope
            import dev.mokkery.matcher.any
        
            fun MokkeryMatcherScope.matcher(): Int {
                val matcher = any<Int>()
                matcher$expression
                return matcher
            }
        """.trimIndent()
            ).assertSingleError("Operators cannot be used with matchers, but '$operator' operator is used")
        }
    }

    @Test
    fun testForbidsTryCatchExpressionReturningMatcher() {
        compileJvm(
            """
            import dev.mokkery.matcher.MokkeryMatcherScope
            import dev.mokkery.matcher.any
        
            fun MokkeryMatcherScope.matcher(): Int {
                return try {
                    any<Int>()
                } catch (e: Exception) {
                    0
                }
            }
        """.trimIndent()
        ).assertSingleError("Returning matchers from try/catch is not supported.")
    }

    @Test
    fun testAllowsTryCatchUsingMatcher() {
        val result = compileJvm(
            """
            import dev.mokkery.matcher.MokkeryMatcherScope
            import dev.mokkery.matcher.any
        
            fun MokkeryMatcherScope.matcher(): Int {            
                return try {
                    any<Int>()
                    1
                } catch (e: Exception) {
                    0
                }
            }
        """.trimIndent()
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertTrue(result.messages.isEmpty())
    }


    @Test
    fun testForbidsPassingMatcherToNonMatcherParam() {
        compileJvm(
            """
            import dev.mokkery.matcher.MokkeryMatcherScope
            import dev.mokkery.matcher.any
            import dev.mokkery.matcher.gte
        
            fun <T : Comparable<T>> MokkeryMatcherScope.matcher(): T = gte(any())
        """.trimIndent()
        ).assertSingleError("'value: T' does not accept matchers, but matcher argument is given. Mark parameter with @Matcher annotation, or use regular values.")
    }

    @Test
    fun testForbidsMethodInvocationsOnMatchers() {
        tests(
            "matcher.toString()",
            "any<Int>().toString()",
            "matcher > 1",
            "any<Int>() > 1",
        ) { expression ->
            compileJvm(
                """
                import dev.mokkery.matcher.MokkeryMatcherScope
                import dev.mokkery.matcher.any
            
                fun MokkeryMatcherScope.matcher(): Int {
                    val matcher = any<Int>()
                    $expression
                    return matcher
                }
            """.trimIndent()
            ).assertSingleError("Invoking methods on matchers is illegal.")
        }
    }

    @Test
    fun testForbidsPassingMatchersToFunctions() {
        tests(
            "listOf(matcher)",
            "listOf(any<Int>())",
            "matcher.toHexString()",
            "any<Int>().toHexString()",
        ) { expression ->
            compileJvm(
                """
                import dev.mokkery.matcher.MokkeryMatcherScope
                import dev.mokkery.matcher.any
            
                fun MokkeryMatcherScope.matcher(): Int {            
                    val matcher = any<Int>()
                    $expression
                    return matcher
                }
            """.trimIndent()
            ).assertSingleError("Matchers can be only passed to mock methods.")
        }
    }

    @Test
    fun testForbidsPassingMatchersToMethods() {
        compileJvm(
            """
                import dev.mokkery.matcher.MokkeryMatcherScope
                import dev.mokkery.matcher.any
                import dev.mokkery.mock
            
                interface Foo {
                    fun foo(i: Int)
                }

                val mock = mock<Foo>()

                fun MokkeryMatcherScope.matcher(): Int {            
                    val matcher = any<Int>()
                    mock.foo(matcher)
                    return matcher
                }
            """.trimIndent()
        ).assertSingleError("Passing matchers to methods is not legal inside matcher builders.")
    }

    @Test
    fun testForbidsUsingTemplatingInsideMatchers() {
        tests("every", "everySuspend", "verify", "verifySuspend") { templatingFunc ->
            compileJvm(
                """
                import dev.mokkery.matcher.MokkeryMatcherScope
                import dev.mokkery.matcher.any
                import dev.mokkery.$templatingFunc

                fun MokkeryMatcherScope.matcher(): Int {            
                    $templatingFunc {  }
                    return any<Int>()
                }
            """.trimIndent()
            ).assertSingleError("'$templatingFunc' calls cannot be nested in templating functions or matcher declarations.")
        }
    }

    @Test
    fun testForbidsUsingMatchersInLambdas() {
        compileJvm(
            """
                import dev.mokkery.matcher.MokkeryMatcherScope
                import dev.mokkery.matcher.logical.not
                import dev.mokkery.every

                fun MokkeryMatcherScope.matcher(): Int {              
                    return 1.let { not(it) }
                }
            """.trimIndent()
        ).assertSingleError("Matchers cannot be used in functions declared inside templating functions or matcher declarations, but used in 'let@fun <anonymous>(it: Int): Int <inline=Inline, kind=EXACTLY_ONCE>'. If you're trying to invoke a method with an extension receiver or context parameters, use the `dev.mokkery.templating.ext` or `dev.mokkery.templating.ctx` functions instead.")
    }

    @Test
    fun testForbidsUsingMatchersInNestedFunctions() {
        compileJvm(
            """
                import dev.mokkery.matcher.MokkeryMatcherScope
                import dev.mokkery.matcher.any
                import dev.mokkery.every

                fun MokkeryMatcherScope.matcher(): Int {     
                    fun nested() = any<Int>()
                    return nested()
                }
            """.trimIndent()
        ).assertSingleError("Matchers cannot be used in functions declared inside templating functions or matcher declarations, but used in 'fun nested(): Int'. If you're trying to invoke a method with an extension receiver or context parameters, use the `dev.mokkery.templating.ext` or `dev.mokkery.templating.ctx` functions instead.")
    }

    @Test
    fun testForbidsUsingMatchersInNestedClasses() {
        compileJvm(
            """
                import dev.mokkery.matcher.MokkeryMatcherScope
                import dev.mokkery.matcher.any
                import dev.mokkery.every

                fun MokkeryMatcherScope.matcher(): Int {     
                    class Foo {
                        fun nested() = any<Int>()
                    }
                    return Foo().nested()
                }
            """.trimIndent()
        ).assertSingleError("Matchers cannot be used in classes declared inside templating functions or matcher declarations, but used in 'class Foo : Any'.")
    }

    @Test
    fun testForbidsUsingMatchersInNestedAnonymousObjects() {
        compileJvm(
            """
                import dev.mokkery.matcher.MokkeryMatcherScope
                import dev.mokkery.matcher.any
                import dev.mokkery.every

                fun MokkeryMatcherScope.matcher(): Int {     
                    val obj = object {
                        fun nested() = any<Int>()
                    }
                    return obj.nested()
                }
            """.trimIndent()
        ).assertSingleError("Matchers cannot be used in classes declared inside templating functions or matcher declarations, but used in 'object : Any'.")
    }

    @Test
    fun testForbidsMatcherAsIfConditionExpression() {
        tests(
            "matcher",
            "any<Boolean>()",
        ) { expression ->

            compileJvm(
                """
                import dev.mokkery.matcher.MokkeryMatcherScope
                import dev.mokkery.matcher.any
            
                fun MokkeryMatcherScope.matcher(): Boolean {            
                    val matcher = any<Boolean>()
                    if ($expression) Unit
                    return matcher
                }
            """.trimIndent()
            ).assertSingleError("Matcher cannot be used as a condition.")
        }
    }

    @Test
    fun testForbidsMatcherAsWhenBranchConditionExpression() {
        tests(
            "matcher",
            "any<Boolean>()",
        ) { expression ->

            compileJvm(
                """
                import dev.mokkery.matcher.MokkeryMatcherScope
                import dev.mokkery.matcher.any
            
                fun MokkeryMatcherScope.matcher(): Boolean {            
                    val matcher = any<Boolean>()
                    when {
                        $expression -> Unit
                        else -> Unit
                    }
                    return matcher
                }
            """.trimIndent()
            ).assertSingleError("Matcher cannot be used as a condition.")
        }
    }

    @Test
    fun testForbidsMatcherAsWhenVariableExpression() {
        tests(
            "matcher",
            "any<Int>()",
            "val m = any<Int>()",
            "val m = matcher",
        ) { expression ->

            compileJvm(
                """
                import dev.mokkery.matcher.MokkeryMatcherScope
                import dev.mokkery.matcher.any
            
                fun MokkeryMatcherScope.matcher(): Int {            
                    val matcher = any<Int>()
                    when ($expression) {
                        else -> Unit
                    }
                    return matcher
                }
            """.trimIndent()
            ).assertSingleError("Matcher cannot be used as a when subject.")
        }
    }

    @Test
    fun testForbidsMatcherAsLoopConditionExpression() {
        tests(
            "while(matcher) Unit",
            "while(any<Boolean>()) Unit",
            "do { } while(matcher)",
            "do { } while(any<Boolean>())",
        ) { expression ->

            compileJvm(
                """
                import dev.mokkery.matcher.MokkeryMatcherScope
                import dev.mokkery.matcher.any
            
                fun MokkeryMatcherScope.matcher(): Boolean {            
                    val matcher = any<Boolean>()
                    $expression
                    return matcher
                }
            """.trimIndent()
            ).assertSingleError("Matcher cannot be used as a condition.")
        }
    }

    @Test
    fun testForbidsMatcherAssignmentToOutOfScopeVariable() {
        compileJvm(
            """
                import dev.mokkery.matcher.MokkeryMatcherScope
                import dev.mokkery.matcher.any
            
                var foo: Int = 0

                fun MokkeryMatcherScope.matcher(): Int {            
                    foo = any<Int>()
                    return foo
                }
            """.trimIndent()
        ).assertSingleError("'var foo: Int' is defined outside the templating scope and cannot be assigned using matchers.")
    }

    @Test
    fun testForbidsMatcherAssignmentToVariableThatIsNotInitializedWithMatcher() {
        compileJvm(
            """
                import dev.mokkery.matcher.MokkeryMatcherScope
                import dev.mokkery.matcher.any
            
                fun MokkeryMatcherScope.matcher(): Int {      
                    var foo: Int = 0
                    foo = any<Int>()
                    return foo
                }
            """.trimIndent()
        ).assertSingleError("'local var foo: Int' is not initialized with a matcher and therefore cannot be reassigned using one.")
    }

    @Test
    fun testForbidsRegisteringCompositeMatcherUsingMatches() {
        compileJvm(
            """
                import dev.mokkery.matcher.MokkeryMatcherScope
                import dev.mokkery.matcher.matches
                import dev.mokkery.matcher.ArgMatcher
                
                @OptIn(dev.mokkery.annotations.DelicateMokkeryApi::class)
                class FooCompositeMatcher : ArgMatcher.Composite<Int> {
                
                    override fun matches(arg: Int): Boolean = true
        
                    override fun capture(value: Int): Unit = Unit
                }

                fun MokkeryMatcherScope.matcher(): Int = matches(FooCompositeMatcher())
            """.trimIndent()
        ).assertSingleError("`dev.mokkery.matcher.matches` cannot be used with `ArgMatcher.Composite` matchers. To register composite matcher use `dev.mokkery.matcher.matchesComposite`.")
    }
}
