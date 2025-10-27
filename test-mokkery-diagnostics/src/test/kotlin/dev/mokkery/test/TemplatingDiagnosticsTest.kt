package dev.mokkery.test

import kotlin.test.Test

class TemplatingDiagnosticsTest {
    @Test
    fun testForbidsPassingLambdaVariablesToEvery() {
        compileJvm(
            """
            import dev.mokkery.every
            import dev.mokkery.templating.MokkeryTemplatingScope
            
            fun test(block: MokkeryTemplatingScope.() -> Int) {
                every(block)
            }
            """.trimIndent()
        ).assertSingleError("Argument passed to 'every' for param 'block: MokkeryTemplatingScope.() -> T' must be a lambda expression.")
    }

    @Test
    fun testForbidsPassingLambdaVariablesToEverySuspend() {
        compileJvm(
            """
            import dev.mokkery.everySuspend
            import dev.mokkery.templating.MokkeryTemplatingScope
            
            fun test(block: suspend MokkeryTemplatingScope.() -> Int) {
                everySuspend(block)
            }
            """.trimIndent()
        ).assertSingleError("Argument passed to 'everySuspend' for param 'block: suspend MokkeryTemplatingScope.() -> T' must be a lambda expression.")
    }

    @Test
    fun testForbidsPassingLambdaVariablesToVerify() {
        compileJvm(
            """
            import dev.mokkery.verify
            import dev.mokkery.templating.MokkeryTemplatingScope
            
            fun test(block: MokkeryTemplatingScope.() -> Unit) {
                verify(block = block)
            }
            """.trimIndent()
        ).assertSingleError("Argument passed to 'verify' for param 'block: MokkeryTemplatingScope.() -> Unit' must be a lambda expression.")
    }

    @Test
    fun testForbidsPassingLambdaVariablesToVerifySuspend() {
        compileJvm(
            """
            import dev.mokkery.verifySuspend
            import dev.mokkery.templating.MokkeryTemplatingScope
            
            fun test(block: suspend MokkeryTemplatingScope.() -> Unit) {
                verifySuspend(block = block)
            }
            """.trimIndent()
        ).assertSingleError("Argument passed to 'verifySuspend' for param 'block: suspend MokkeryTemplatingScope.() -> Unit' must be a lambda expression.")
    }
}
