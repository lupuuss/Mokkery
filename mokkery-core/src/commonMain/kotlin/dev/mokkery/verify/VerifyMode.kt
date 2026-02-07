package dev.mokkery.verify

import dev.mokkery.annotations.InternalMokkeryApi

/**
 * Determines how strict calls verification should be.
 */
public sealed class VerifyMode {

    public companion object {

        /**
         * Verifies that none of the specified calls occurred.
         */
        public val not: VerifyMode get() = VerifyModeInternals.Not

        /**
         * Verifies that given calls occurred in specified order. It allows other calls in between.
         */
        public val order: VerifyMode get() = VerifyModeInternals.Order

        /**
         * Verifies that given calls occurred in exact same order and there is no more calls.
         */
        public val exhaustiveOrder: VerifyMode = VerifyModeInternals.ExhaustiveOrder

        /**
         * Verifies that each function call occurred at least once in any order and there is no more calls.
         */
        public val exhaustive: VerifyMode = VerifyModeInternals.Exhaustive

        /**
         * Verifies that each function call occurred at least once in any order. It allows unverified calls.
         */
        public val soft: VerifyMode = VerifyModeInternals.Soft(atLeast = 1, atMost = Int.MAX_VALUE)

        /**
         * Verifies that each function call occurred at least [n] times. It allows unverified calls.
         */
        public fun atLeast(n: Int): VerifyMode = VerifyModeInternals.Soft(atLeast = n, atMost = Int.MAX_VALUE)

        /**
         * Verifies that each function call occurred at least once and at most [n] times. It allows unverified calls.
         */
        public fun atMost(n: Int): VerifyMode = VerifyModeInternals.Soft(atLeast = 1, atMost = n)

        /**
         * Verifies that each function call occurred exactly [n] times. It allows unverified calls.
         */
        public fun exactly(n: Int): VerifyMode = VerifyModeInternals.Soft(atLeast = n, atMost = n)

        /**
         * Verifies that number of calls for each function is within the given [range]. It allows unverified calls.
         */
        public fun inRange(range: IntRange): VerifyMode = VerifyModeInternals.Soft(atLeast = range.first, atMost = range.last)
    }
}

@InternalMokkeryApi
public object VerifyModeInternals {

    /**
     * Verify mode used by [VerifyMode.not].
     */
    @InternalMokkeryApi
    public object Not : VerifyMode()

    /**
     * Verify mode used by [VerifyMode.order]
     */
    @InternalMokkeryApi
    public object Order : VerifyMode()

    /**
     * Verify mode used by [VerifyMode.exhaustiveOrder]
     */
    @InternalMokkeryApi
    public object ExhaustiveOrder : VerifyMode()

    /**
     * Verify mode used by [VerifyMode.exhaustive]
     */
    @InternalMokkeryApi
    public object Exhaustive : VerifyMode()

    /**
     * Verify mode used by [VerifyMode.soft], [VerifyMode.atLeast], [VerifyMode.atMost], [VerifyMode.exactly], [VerifyMode.inRange]
     */
    @InternalMokkeryApi
    public data class Soft(val atLeast: Int, val atMost: Int) : VerifyMode()

}
