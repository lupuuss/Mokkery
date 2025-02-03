package dev.mokkery

import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.GlobalMokkeryScope
import dev.mokkery.internal.MokkeryPluginNotAppliedException
import dev.mokkery.internal.context.MocksRegistry
import dev.mokkery.internal.context.mocksRegistry

/**
 * Returns all mocks from this [MokkeryTestsScope].
 */
public val MokkeryTestsScope.mocks: Set<Any>
    get() = mocksRegistry.mocks

/**
 * Registers given [mock] in this [MokkeryTestsScope].
 *
 * Usually, it should not be necessary to register mocks manually. Use [MokkeryTestsScope.mock],
 * [MokkeryTestsScope.mockMany] and [MokkeryTestsScope.spy] overloads to create mocks that are registered automatically.
 */
public fun <T : Any> MokkeryTestsScope.registerMock(mock: T): T = mock.also(mocksRegistry::register)

/**
 * A scope for tests that use Mokkery mocks, enabling automation and enhanced exhaustiveness checks.
 *
 * ### How It Works
 *
 * Every mock involved in your test must be registered in this scope.
 * This happens automatically when using [MokkeryTestsScope.mock], [MokkeryTestsScope.mockMany], or
 * [MokkeryTestsScope.spy]. Alternatively, you can register mocks manually with [registerMock].
 *
 * ### Benefits
 *
 * Mokkery provides [MokkeryTestsScope.verify] and [MokkeryTestsScope.verifySuspend]. When used with
 * an exhaustive [dev.mokkery.verify.VerifyMode], these ensure that **all** registered mocks (not just
 * those inside the verify block) are checked for exhaustiveness.
 *
 * Additionally, you can call [MokkeryTestsScope.verifyNoMoreCalls] to ensure that all mocks in the scope
 * have had their calls fully verified.
 *
 * ### Example
 *
 * Consider the following test class:
 * ```kotlin
 * class ClassTest {
 *
 *      private val a = mock<A>()
 *      private val b = mock<B>()
 *
 *      @Test
 *      fun test() {
 *          a.call(1)
 *          b.call(2)
 *          // Verification passes
 *          verify(exhaustive) {
 *              a.call(1)
 *          }
 *      }
 * }
 * ```
 * Normally, this test passes because Mokkery only checks for the exhaustiveness of mocks used inside the verify block.
 *
 * However, if we mark the test class with [MokkeryTestsScope], the behavior changes:
 * ```kotlin
 * class ClassTest : MokkeryTestsScope {
 *
 *      private val a = mock<A>()
 *      private val b = mock<B>()
 *
 *      @Test
 *      fun test() {
 *          a.call(1)
 *          b.call(2)
 *          // Verification fails
 *          // Unverified call: b.call(2)
 *          verify(exhaustive) {
 *              a.call(1)
 *          }
 *      }
 * }
 * ```
 *
 * In this test, [verify] leverages the scope to improve exhaustiveness checks.
 *
 * If you cannot mark your test class with [MokkeryTestsScope], you can create a scope manually using [MokkeryTestsScope]:
 *
 * ```kotlin
 * with(MokkeryTestsScope()) {
 *      val a = mock<A>()
 *      val b = mock<B>()
 *      a.call(1)
 *      b.call(2)
 *      // Verification fails
 *      // Unverified call: b.call(2)
 *      verify(exhaustive) {
 *          a.call(1)
 *      }
 * }
 * ```
 */
public interface MokkeryTestsScope : MokkeryScope {

    override val mokkeryContext: MokkeryContext
        get() = throw MokkeryPluginNotAppliedException()
}

/**
 * Creates [MokkeryTestsScope] instance with given extra [context].
 *
 * Check interface documentation for details.
 */
public fun MokkeryTestsScope(context: MokkeryContext = MokkeryContext.Empty): MokkeryTestsScope {
    return object : MokkeryTestsScope {
        override val mokkeryContext = GlobalMokkeryScope.mokkeryContext + MocksRegistry() + context

        override fun toString(): String = "MokkeryTestsScope(mokkeryContext=$mokkeryContext)"
    }
}
