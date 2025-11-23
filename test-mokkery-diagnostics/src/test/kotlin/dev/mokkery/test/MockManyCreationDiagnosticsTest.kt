package dev.mokkery.test

import com.tschuchort.compiletesting.KotlinCompilation
import kotlin.test.Test
import kotlin.test.assertEquals

class MockManyCreationDiagnosticsTest {

    @Test
    fun testForbidsPrimitiveTypes() = tests("Int", "Double") { type ->
        compileJvm(
            """
            import dev.mokkery.mockMany
            
            fun main() {
                mockMany<$type, AutoCloseable>()
            }
            """.trimIndent()
        ).assertSingleError("Type '$type' is primitive and cannot be used with 'mockMany'.")
    }

    @Test
    fun testForbidsFinalTypes() {
        compileJvm(
            """
            import dev.mokkery.mockMany
        
            class FinalClass

            fun main() {
                mockMany<FinalClass, AutoCloseable>()
            }
            """.trimIndent()
        ).assertSingleError("Type 'FinalClass' is final and cannot be used with 'mockMany'.")
    }

    @Test
    fun testForbidsSealedTypes() = tests("sealed interface", "sealed class") { type ->
        compileJvm(
            """
            import dev.mokkery.mockMany
        
            $type SealedInterface

            fun main() {
                mockMany<SealedInterface, AutoCloseable>()
            }
            """.trimIndent()
        ).assertSingleError("Type 'SealedInterface' is sealed and cannot be used with 'mockMany'.")

    }

    @Test
    fun testForbidsIndirectTypes() {
        compileJvm(
            """
            import dev.mokkery.mockMany
        
            inline fun <reified T : Any> myMock() = mockMany<T, AutoCloseable>()
            
            fun main() {
                myMock<List<Int>>()
            }
            """.trimIndent()
        ).assertSingleError("'T (of fun <T : Any> myMock)' is a type parameter. Specific type expected for a 'mockMany' call.")
    }

    @Test
    fun testForbidsFinalMembers() {
        compileJvm(
            """
            import dev.mokkery.mockMany
        
            abstract class AbstractClass {
                fun finalMethod() = Unit
            }

            fun main() {
                mockMany<AbstractClass, AutoCloseable>()
            }
            """.trimIndent()
        ).assertSingleError("Type 'AbstractClass' has final members and cannot be used with 'mockMany'. Final members: fun finalMethod(): Unit")
    }

    @Test
    fun testRequiresPublicConstructor() {
        compileJvm(
            """
            import dev.mokkery.mockMany
        
            abstract class AbstractClass private constructor()

            fun main() {
                mockMany<AbstractClass, AutoCloseable>()
            }
            """.trimIndent()
        ).assertSingleError("Type 'AbstractClass' has no public constructor and cannot be used with 'mockMany'.")
    }

    @Test
    fun testForbidsTypesDuplicates() {
        compileJvm(
            """
            import dev.mokkery.mockMany
        
            fun main() {
                mockMany<AutoCloseable, AutoCloseable>()
            }
            """.trimIndent()
        ).assertSingleError("Type 'AutoCloseable' for 'mockMany' must occur only once, but it occurs 2 times.")
    }

    @Test
    fun testForbidsGenericTypesDuplicates() {
        compileJvm(
            """
            import dev.mokkery.mockMany
        
            fun main() {
                mockMany<List<Int>, List<String>, List<Double>>()
            }
            """.trimIndent()
        ).assertSingleError("Type 'List<E (of interface List<out E>)>' for 'mockMany' must occur only once, but it occurs 3 times.")
    }

    @Test
    fun testForbidsMultipleSuperclasses() {
        compileJvm(
            """
            import dev.mokkery.mockMany
    
            abstract class AbstractClass1
            abstract class AbstractClass2
            abstract class AbstractClass3

            fun main() {
                mockMany<AbstractClass1, AbstractClass2, AbstractClass3>()
            }
            """.trimIndent()
        ).assertSingleError("Only one super class is acceptable for 'mockMany' type. Detected super classes: AbstractClass1, AbstractClass2, AbstractClass3")
    }

    @Test
    fun testAllowsFunctionOnNonJs() {
        val result = compileJvm(
            """
            import dev.mokkery.mockMany

            fun main() {
                mockMany<AutoCloseable, () -> Int>()
            }
            """.trimIndent()
        )
        assert(result.messages.isEmpty())
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}
