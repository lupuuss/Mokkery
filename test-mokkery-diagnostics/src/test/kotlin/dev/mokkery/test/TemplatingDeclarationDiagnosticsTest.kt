package dev.mokkery.test

import kotlin.test.Test

class TemplatingDeclarationDiagnosticsTest {

    @Test
    fun testForbidsDeclaringFunctionsAcceptingMokkeryTemplatingScopeAsExtensionReceiver() {
        compileJvm(
            """
            import dev.mokkery.verify
            import dev.mokkery.templating.MokkeryTemplatingScope
            
            fun MokkeryTemplatingScope.foo() = Unit
            """.trimIndent()
        ).assertSingleError("Extracting templating to separate functions is currently illegal. Templating must be performed directly inside lambda passed to `every`/`verify`.")
    }

    @Test
    fun testForbidsDeclaringFunctionsAcceptingMokkeryTemplatingScopeAsValueParameters() {
        compileJvm(
            """
            import dev.mokkery.verify
            import dev.mokkery.templating.MokkeryTemplatingScope
            
            fun foo(param: MokkeryTemplatingScope) = Unit
            """.trimIndent()
        ).assertSingleError("Extracting templating to separate functions is currently illegal. Templating must be performed directly inside lambda passed to `every`/`verify`.")
    }

    @Test
    fun testForbidsDeclaringFunctionsAcceptingMokkeryTemplatingScopeAsContextParameters() {
        compileJvm(
            """
            import dev.mokkery.verify
            import dev.mokkery.templating.MokkeryTemplatingScope
            
            context(ctx: MokkeryTemplatingScope)
            fun foo() = Unit
            """.trimIndent()
        ).assertSingleError("Extracting templating to separate functions is currently illegal. Templating must be performed directly inside lambda passed to `every`/`verify`.")
    }

    @Test
    fun testForbidsDeclaringLambdasAcceptingMokkeryTemplatingScopeAsExtensionReceiver() {
        compileJvm(
            """
            import dev.mokkery.verify
            import dev.mokkery.templating.MokkeryTemplatingScope
            
            fun main() {
                val foo: MokkeryTemplatingScope.() -> Unit = { }
            }
            """.trimIndent()
        ).assertSingleError("Extracting templating to separate functions is currently illegal. Templating must be performed directly inside lambda passed to `every`/`verify`.")
    }

    @Test
    fun testForbidsDeclaringLambdasAcceptingMokkeryTemplatingScopeAsValueParameters() {
        compileJvm(
            """
            import dev.mokkery.verify
            import dev.mokkery.templating.MokkeryTemplatingScope

            fun main() {
                val foo: (MokkeryTemplatingScope) -> Unit = { }
            }
            """.trimIndent()
        ).assertSingleError("Extracting templating to separate functions is currently illegal. Templating must be performed directly inside lambda passed to `every`/`verify`.")
    }

    @Test
    fun testForbidsDeclaringLambdasAcceptingMokkeryTemplatingScopeAsContextParameters() {
        compileJvm(
            """
            import dev.mokkery.verify
            import dev.mokkery.templating.MokkeryTemplatingScope
            
            fun main() {
                val foo: context(MokkeryTemplatingScope) () -> Unit = { }
            }
            """.trimIndent()
        ).assertSingleError("Extracting templating to separate functions is currently illegal. Templating must be performed directly inside lambda passed to `every`/`verify`.")
    }
}

