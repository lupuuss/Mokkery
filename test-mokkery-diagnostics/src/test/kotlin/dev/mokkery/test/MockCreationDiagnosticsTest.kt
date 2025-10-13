package dev.mokkery.test

import kotlin.test.Test

class MockCreationDiagnosticsTest {

    @Test
    fun testForbidsPrimitiveTypes() = tests("Int", "Double") { type ->
        compileJvm(
            """
            import dev.mokkery.mock
            
            fun main() {
                mock<$type>()
            }
            """.trimIndent()
        ).assertSingleError("Type '$type' is primitive and cannot be used with 'mock'.")
    }

    @Test
    fun testForbidsFinalTypes() {
        compileJvm(
            """
            import dev.mokkery.mock
        
            class FinalClass

            fun main() {
                mock<FinalClass>()
            }
            """.trimIndent()
        ).assertSingleError("Type 'FinalClass' is final and cannot be used with 'mock'.")
    }

    @Test
    fun testForbidsSealedTypes() = tests("sealed interface", "sealed class") { type ->
        compileJvm(
            """
            import dev.mokkery.mock
        
            $type SealedInterface

            fun main() {
                mock<SealedInterface>()
            }
            """.trimIndent()
        ).assertSingleError("Type 'SealedInterface' is sealed and cannot be used with 'mock'.")

    }

    @Test
    fun testForbidsIndirectTypes() {
        compileJvm(
            """
            import dev.mokkery.mock
        
            inline fun <reified T : Any> myMock() = mock<T>()
            
            fun main() {
                myMock<AutoCloseable>()
            }
            """.trimIndent()
        ).assertSingleError("'T (of fun <T : Any> myMock)' is a type parameter. Specific type expected for a 'mock' call.")
    }

    @Test
    fun testForbidsFinalMembers() {
        compileJvm(
            """
            import dev.mokkery.mock
        
            abstract class AbstractClass {
                fun finalMethod() = Unit
            }

            fun main() {
                mock<AbstractClass>()
            }
            """.trimIndent()
        ).assertSingleError("Type 'AbstractClass' has final members and cannot be used with 'mock'. Final members: fun finalMethod(): Unit")
    }

    @Test
    fun testRequiresPublicConstructor() {
        compileJvm(
            """
            import dev.mokkery.mock
        
            abstract class AbstractClass private constructor()

            fun main() {
                mock<AbstractClass>()
            }
            """.trimIndent()
        ).assertSingleError("Type 'AbstractClass' has no public constructor and cannot be used with 'mock'.")
    }

}
