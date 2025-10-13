package dev.mokkery.test

import kotlin.test.Test

class MatchersDeclarationDiagnosticsTest {

    @Test
    fun testForbidsExternalModifier() {
        compileJvm(
            """
            import dev.mokkery.matcher.MokkeryMatcherScope
        
            external fun <T> MokkeryMatcherScope.matcher(): T
        """.trimIndent()
        ).assertSingleError("Matcher must not be external.")
    }

    @Test
    fun testRequiresFinalModality() {
        compileJvm(
            """
            import dev.mokkery.matcher.MokkeryMatcherScope
        
            interface Foo {
            
                fun <T> MokkeryMatcherScope.matcher(): T
            }
        """.trimIndent()
        ).assertSingleError("Matcher must be final.")
    }

    @Test
    fun testForbidsLambdas() {
        compileJvm(
            """
            import dev.mokkery.matcher.MokkeryMatcherScope
        
            val matcher: MokkeryMatcherScope.() -> Int = { 1 }
        """.trimIndent()
        ).assertSingleError("Matcher must be a regular function.")
    }

    @Test
    fun testForbidsMatcherAnnotationOnMatcher() {
        tests(
            "context(@Matcher m: ArgMatcher<T>) fun <T> MokkeryMatcherScope.matcher(): T = any()" to "ArgMatcher<T (of context() fun <T> MokkeryMatcherScope.matcher)>",
            "fun <T> @receiver:Matcher ArgMatcher<T>.matcher(scope: MokkeryMatcherScope): T = scope.any()" to "ArgMatcher<T (of fun <T> ArgMatcher<T>.matcher)>",
            "fun <T> MokkeryMatcherScope.matcher(@Matcher m: ArgMatcher<T>): T = any()" to "ArgMatcher<T (of fun <T> MokkeryMatcherScope.matcher)>",
        ) { (declaration, renderedType) ->
            compileJvm(
                    """
                import dev.mokkery.matcher.MokkeryMatcherScope
                import dev.mokkery.matcher.ArgMatcher
                import dev.mokkery.matcher.any
                import dev.mokkery.annotations.Matcher
                
                $declaration
            """.trimIndent()
            ).assertSingleError("Parameter of type '$renderedType' cannot be marked with @dev.mokkery.annotations.Matcher")
        }
    }

    @Test
    fun testForbidsMatcherAnnotationOnMokkeryMatcherScope() {
        tests(
            "fun <T> @receiver:Matcher MokkeryMatcherScope.matcher(): T = any()",
            "fun <T> matcher(@Matcher scope: MokkeryMatcherScope): T = scope.any()",
            "context(@Matcher scope: MokkeryMatcherScope) fun <T> matcher(): T = scope.any()",
        ) { declaration ->
            compileJvm(
                """
            import dev.mokkery.matcher.MokkeryMatcherScope
            import dev.mokkery.matcher.any
            import dev.mokkery.annotations.Matcher
        
            $declaration
        """.trimIndent()
            ).assertSingleError("Parameter of type 'MokkeryMatcherScope' cannot be marked with @dev.mokkery.annotations.Matcher")
        }
    }
}
