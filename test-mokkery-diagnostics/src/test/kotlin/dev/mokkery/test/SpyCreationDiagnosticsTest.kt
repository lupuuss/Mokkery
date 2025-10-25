package dev.mokkery.test

import kotlin.test.Test

class SpyCreationDiagnosticsTest {

    @Test
    fun testForbidsPrimitiveTypes() = tests("Int", "Double") { type ->
        compileJvm(
            """
            import dev.mokkery.spy
           
            val spied: $type = error("init")
            
            fun main() {
                spy<$type>(spied)
            }
            """.trimIndent()
        ).assertSingleError("Type '$type' is primitive and cannot be used with 'spy'.")
    }

    @Test
    fun testForbidsFinalTypes() {
        compileJvm(
            """
            import dev.mokkery.spy
        
            class FinalClass

            fun main() {
                spy(FinalClass())
            }
            """.trimIndent()
        ).assertSingleError("Type 'FinalClass' is final and cannot be used with 'spy'.")
    }

    @Test
    fun testForbidsSealedTypes() = tests("sealed interface", "sealed class") { type ->
        compileJvm(
            """
            import dev.mokkery.spy
        
            $type SealedInterface
            
            val spied: SealedInterface = error("init")

            fun main() {
                spy(spied)
            }
            """.trimIndent()
        ).assertSingleError("Type 'SealedInterface' is sealed and cannot be used with 'spy'.")

    }

    @Test
    fun testForbidsIndirectType() {
        compileJvm(
            """
            import dev.mokkery.spy
        
            inline fun <reified T : Any> mySpy(obj: T) = spy<T>(obj)
            
            fun main() {
                
            }
            """.trimIndent()
        ).assertSingleError("'T (of fun <T : Any> mySpy)' is a type parameter. Specific type expected for a 'spy' call.")
    }

    @Test
    fun testForbidsFinalMembers() {
        compileJvm(
            """
            import dev.mokkery.spy
        
            abstract class AbstractClass {
                fun finalMethod() = Unit
            }

            val spied: AbstractClass = error("init")

            fun main() {
                spy(spied)
            }
            """.trimIndent()
        ).assertSingleError("Type 'AbstractClass' has final members and cannot be used with 'spy'. Final members: fun finalMethod(): Unit")
    }

    @Test
    fun testRequiredPublicConstructor() {
        compileJvm(
            """
            import dev.mokkery.spy
        
            abstract class AbstractClass private constructor()

            val spied: AbstractClass = error("init")

            fun main() {
                spy(spied)
            }
            """.trimIndent()
        ).assertSingleError("Type 'AbstractClass' has no public constructor and cannot be used with 'spy'.")
    }

}
