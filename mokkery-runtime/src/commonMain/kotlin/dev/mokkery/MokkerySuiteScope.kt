package dev.mokkery

import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.internal.GlobalMokkeryScope
import dev.mokkery.internal.MokkerySuiteScopeNotImplementedException
import dev.mokkery.internal.context.MocksRegistry
import dev.mokkery.internal.utils.reverseResolvedInstances

/**
 * Returns all mocks from this [MokkerySuiteScope].
 */
public val MokkerySuiteScope.mocks: List<Any>
    get() = mokkeryContext
        .require(MocksRegistry)
        .mocks
        .reverseResolvedInstances
        .toList()

/**
 * A scope for a test suite that uses Mokkery mocks. It enables automation and strict exhaustiveness checks.
 *
 * Every mock created with [MokkerySuiteScope.mock], [MokkerySuiteScope.mockMany], or
 * [MokkerySuiteScope.spy] becomes a part of given scope.
 *
 * Mokkery provides [MokkerySuiteScope.verify] and [MokkerySuiteScope.verifySuspend]. When used with
 * an exhaustive [dev.mokkery.verify.VerifyMode], **all** mocks in this scope (not just those inside the verify block)
 * are checked for exhaustiveness.
 *
 * Additionally, you can call [MokkerySuiteScope.verifyNoMoreCalls] to ensure that all mocks in the scope
 * have had their calls fully verified.
 *
 * ### Regular test classes
 *
 * For regular test classes, mark them with [MokkerySuiteScope] interface.
 * [MokkerySuiteScope] has a default [mokkeryContext] implementation that throws an exception.
 * This property is meant to be automatically overridden by the compiler plugin when not explicitly
 * set by the user. Manual implementation is **not recommended**.
 *
 * The compiler plugin overrides this property only in classes that directly inherit from [MokkerySuiteScope].
 * If [MokkerySuiteScope] is inherited indirectly, it is not implemented.
 *
 * ```kotlin
 * class ClassTest : MokkerySuiteScope {
 *      private val a = mock<A>()
 *      private val b = mock<B>()
 *
 *      @Test
 *      fun test() {
 *          a.call(1)
 *          b.call(2)
 *          // Verification fails - b.call(2) was not verified
 *          verify(exhaustive) {
 *              a.call(1)
 *          }
 *      }
 * }
 * ```
 *
 * ### Other cases
 * If you cannot mark your test class with [MokkerySuiteScope], create an instance using the [MokkerySuiteScope] function.
 *
 * ```kotlin
 * with(MokkerySuiteScope()) {
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
public interface MokkerySuiteScope : MokkeryScope {

    override val mokkeryContext: MokkeryContext
        get() = throw MokkerySuiteScopeNotImplementedException()
}

/**
 * Creates [MokkerySuiteScope] instance with given extra [context].
 *
 * Check interface documentation for details.
 */
public fun MokkerySuiteScope(context: MokkeryContext = MokkeryContext.Empty): MokkerySuiteScope {
    return object : MokkerySuiteScope {
        override val mokkeryContext = GlobalMokkeryScope.mokkeryContext + MocksRegistry() + context

        override fun toString(): String = "MokkerySuiteScope(mokkeryContext=$mokkeryContext)"
    }
}
