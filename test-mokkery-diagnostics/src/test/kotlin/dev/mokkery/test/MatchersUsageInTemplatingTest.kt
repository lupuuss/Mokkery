package dev.mokkery.test

import com.tschuchort.compiletesting.KotlinCompilation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MatchersUsageInTemplatingTest {

    @Test
    fun testForbidsOperatorOnMatcher() {
        tests(
            "::class" to "::class",
            " == 1" to "==",
            " === 1" to "===",
        ) { (expression, operator) ->
            compileJvm(
                """
            import dev.mokkery.mock
            import dev.mokkery.every
            import dev.mokkery.matcher.any
            
            interface Foo {
                fun foo(arg: Int): Int
            }
            
            fun main() {
                val mock = mock<Foo>()
                every { 
                    any<Int>()$expression
                    mock.foo(any())
                }
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
            import dev.mokkery.mock
            import dev.mokkery.every
            import dev.mokkery.matcher.MokkeryMatcherScope
            import dev.mokkery.matcher.any
            
            interface Foo {
                fun foo(arg: Int): Int
            }
            
            fun main() {
                val mock = mock<Foo>()
                every { 
                    val matcher = any<Int>()
                    matcher$expression
                    mock.foo(matcher)
                }
            }
        """.trimIndent()
            ).assertSingleError("Operators cannot be used with matchers, but '$operator' operator is used")
        }
    }

    @Test
    fun testForbidsTryCatchExpressionReturningMatcher() {
        compileJvm(
            """
            import dev.mokkery.mock
            import dev.mokkery.every
            import dev.mokkery.matcher.any
            
            interface Foo {
                fun foo(arg: Int): Int
            }
            
            fun main() {
                val mock = mock<Foo>()
                every { 
                    val matcher = try {
                        any<Int>()
                    } catch (e: Exception) {
                        0
                    }
                    mock.foo(matcher)
                }
            }
        """.trimIndent()
        ).assertSingleError("Returning matchers from try/catch is not supported.")
    }

    @Test
    fun testAllowsTryCatchUsingMatcher() {
        val result = compileJvm(
            """
            import dev.mokkery.mock
            import dev.mokkery.every
            import dev.mokkery.matcher.any
            
            interface Foo {
                fun foo(arg: Int): Int
            }
            
            fun main() {
                val mock = mock<Foo>()
                every { 
                    val matcher = try {
                        any<Int>()
                        1
                    } catch (e: Exception) {
                        0
                    }
                    mock.foo(matcher)
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
            import dev.mokkery.mock
            import dev.mokkery.every
            import dev.mokkery.matcher.any
            import dev.mokkery.matcher.eq
            
            interface Foo {
                fun foo(arg: Int): Int
            }
            
            fun main() {
                val mock = mock<Foo>()
                every { mock.foo(eq(any())) }
            }
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
                import dev.mokkery.mock
                import dev.mokkery.every
                import dev.mokkery.matcher.any
                
                interface Foo {
                    fun foo(arg: Int): Int
                }
                
                fun main() {
                    val mock = mock<Foo>()
                    every { 
                        val matcher = any<Int>()
                        $expression
                        mock.foo(matcher)
                    }
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
                import dev.mokkery.mock
                import dev.mokkery.every
                import dev.mokkery.matcher.any
                
                interface Foo {
                    fun foo(arg: Int): Int
                }
                
                fun main() {
                    val mock = mock<Foo>()
                    every { 
                        val matcher = any<Int>()
                        $expression
                        mock.foo(matcher)
                    }
                }
                """.trimIndent()
            ).assertSingleError("Matchers can be only passed to mock methods.")
        }
    }

    @Test
    fun testForbidsUsingTemplatingInsideMatchers() {
        tests("every", "everySuspend", "verify", "verifySuspend") { templatingFunc ->
            compileJvm(
                """
                import dev.mokkery.mock
                import dev.mokkery.$templatingFunc
                import dev.mokkery.matcher.any
                
                interface Foo {
                    fun foo(arg: Int): Int
                }
                
                fun main() {
                    val mock = mock<Foo>()
                    $templatingFunc { 
                        $templatingFunc { }
                        mock.foo(any<Int>())
                    }
                }
                """.trimIndent()
            ).assertSingleError("'$templatingFunc' calls cannot be nested in templating functions or matcher declarations.")
        }
    }

    @Test
    fun testForbidsUsingMatchersInLambdas() {
        compileJvm(
            """
            import dev.mokkery.mock
            import dev.mokkery.every
            import dev.mokkery.matcher.any
            import dev.mokkery.matcher.eq
            
            interface Foo {
                fun foo(arg: Int): Int
            }
            
            fun main() {
                val mock = mock<Foo>()
                every { mock.foo(1.let { eq(it) }) }
            }
            """.trimIndent()
        ).assertSingleError("Matchers cannot be used in functions declared inside templating functions or matcher declarations, but used in 'let@fun <anonymous>(it: Int): Int <inline=Inline, kind=EXACTLY_ONCE>'. If you're trying to invoke a method with an extension receiver or context parameters, use the `dev.mokkery.templating.ext` or `dev.mokkery.templating.ctx` functions instead.")
    }

    @Test
    fun testForbidsUsingMatchersInNestedFunctions() {
        compileJvm(
            """
            import dev.mokkery.mock
            import dev.mokkery.every
            import dev.mokkery.matcher.any
            
            interface Foo {
                fun foo(arg: Int): Int
            }
            
            fun main() {
                val mock = mock<Foo>()
                every { 
                    fun nested() = any<Int>()
                    mock.foo(nested()) 
                }
            }
            """.trimIndent()
        ).assertSingleError("Matchers cannot be used in functions declared inside templating functions or matcher declarations, but used in 'fun nested(): Int'. If you're trying to invoke a method with an extension receiver or context parameters, use the `dev.mokkery.templating.ext` or `dev.mokkery.templating.ctx` functions instead.")
    }

    @Test
    fun testForbidsUsingMatchersInNestedClasses() {
        compileJvm(
            """
            import dev.mokkery.mock
            import dev.mokkery.every
            import dev.mokkery.matcher.any
            
            interface Foo {
                fun foo(arg: Int): Int
            }
            
            fun main() {
                val mock = mock<Foo>()
                every { 
                    class Bar {
                        val nested = any<Int>()
                    }
                    mock.foo(Bar().nested) 
                }
            }
            """.trimIndent()
        ).assertSingleError("Matchers cannot be used in classes declared inside templating functions or matcher declarations, but used in 'class Bar : Any'.")
    }

    @Test
    fun testForbidsUsingMatchersInNestedAnonymousObjects() {
        compileJvm(
            """
            import dev.mokkery.mock
            import dev.mokkery.every
            import dev.mokkery.matcher.any
            
            interface Foo {
                fun foo(arg: Int): Int
            }
            
            fun main() {
                val mock = mock<Foo>()
                every { 
                    val bar = object {
                        val nested = any<Int>()
                    }
                    mock.foo(bar.nested) 
                }
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
                import dev.mokkery.mock
                import dev.mokkery.every
                import dev.mokkery.matcher.any
                
                interface Foo {
                    fun foo(arg: Int): Int
                }
                
                fun main() {
                    val mock = mock<Foo>()
                    every { 
                        val matcher = any<Boolean>()
                        if ($expression) Unit
                        mock.foo(any()) 
                    }
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
                import dev.mokkery.mock
                import dev.mokkery.every
                import dev.mokkery.matcher.any
                
                interface Foo {
                    fun foo(arg: Int): Int
                }
                
                fun main() {
                    val mock = mock<Foo>()
                    every { 
                        val matcher = any<Boolean>()
                        when {
                            $expression -> Unit
                            else -> Unit
                        }
                        mock.foo(any()) 
                    }
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
                import dev.mokkery.mock
                import dev.mokkery.every
                import dev.mokkery.matcher.any
                
                interface Foo {
                    fun foo(arg: Int): Int
                }
                
                fun main() {
                    val mock = mock<Foo>()
                    every { 
                        val matcher = any<Int>()
                        when ($expression) {
                            else -> Unit
                        }
                        mock.foo(any()) 
                    }
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
                import dev.mokkery.mock
                import dev.mokkery.every
                import dev.mokkery.matcher.any
                
                interface Foo {
                    fun foo(arg: Int): Int
                }
                
                fun main() {
                    val mock = mock<Foo>()
                    every { 
                        val matcher = any<Boolean>()
                        $expression
                        mock.foo(any()) 
                    }
                }
                """.trimIndent()
            ).assertSingleError("Matcher cannot be used as a condition.")
        }
    }


    @Test
    fun testForbidsMatcherAssignmentToOutOfScopeVariable() {
        compileJvm(
            """
            import dev.mokkery.mock
            import dev.mokkery.every
            import dev.mokkery.matcher.any
            
            interface Foo {
                fun foo(arg: Int): Int
            }

            var foo: Int = 0
            
            fun main() {
                val mock = mock<Foo>()
                every { 
                    foo = any<Int>()
                    mock.foo(foo) 
                }
            }
            """.trimIndent()
        ).assertSingleError("'var foo: Int' is defined outside the templating scope and cannot be assigned using matchers.")
    }

    @Test
    fun testForbidsMatcherAssignmentToVariableThatIsNotInitializedWithMatcher() {
        compileJvm(
            """
            import dev.mokkery.mock
            import dev.mokkery.every
            import dev.mokkery.matcher.any
            
            interface Foo {
                fun foo(arg: Int): Int
            }
            
            fun main() {
                val mock = mock<Foo>()
                every { 
                    var foo: Int = 0
                    foo = any<Int>()
                    mock.foo(foo) 
                }
            }
            """.trimIndent()
        ).assertSingleError("'local var foo: Int' is not initialized with a matcher and therefore cannot be reassigned using one.")
    }

    @Test
    fun testForbidsRegisteringCompositeMatcherUsingMatches() {
        compileJvm(
            """
            import dev.mokkery.mock
            import dev.mokkery.every
            import dev.mokkery.matcher.ArgMatcher
            import dev.mokkery.matcher.matches
            
            interface Foo {
                fun foo(arg: Int): Int
            }
            
            @OptIn(dev.mokkery.annotations.DelicateMokkeryApi::class)
            class FooCompositeMatcher : ArgMatcher.Composite<Int> {
            
                override fun matches(arg: Int): Boolean = true
    
                override fun capture(value: Int): Unit = Unit
            }

            fun main() {
                val mock = mock<Foo>()
                every { mock.foo(matches(FooCompositeMatcher())) }
            }
            """.trimIndent()
        ).assertSingleError("`dev.mokkery.matcher.matches` cannot be used with `ArgMatcher.Composite` matchers. To register composite matcher use `dev.mokkery.matcher.matchesComposite`.")
    }

    @Test
    fun testForbidsUsingMatcherWithFinalMethods() {
        compileJvm(
            """
            import dev.mokkery.mock
            import dev.mokkery.every
            import dev.mokkery.matcher.any
            
            class Foo {
                fun foo(arg: Int): Int = arg
            }
            
            fun main() {
                val mock = Foo()
                every { 
                    mock.foo(any())
                }
            }
            """.trimIndent()
        ).assertSingleError("'fun foo(arg: Int): Int' must not be used with matchers, because it's final and cannot be mocked.")
    }

    @Test
    fun testForbidsUsingMoreThanOneSpreadMatcher() {
        tests(
            "matcher",
            "any()",
        ) { expression ->
            compileJvm(
                """
                import dev.mokkery.mock
                import dev.mokkery.every
                import dev.mokkery.matcher.any
                
                interface Foo {
                    fun foo(vararg args: Int): Int
                }
                
                fun main() {
                    val mock = mock<Foo>()
                    every { 
                        val matcher = any<IntArray>()
                        mock.foo(1, *any(), *$expression, 10) 
                    }
                }
                """.trimIndent()
            ).assertSingleError("Only one vararg matcher is allowed.")
        }
    }
}
