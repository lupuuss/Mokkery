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
        public val not: VerifyMode get() = NotVerifyMode

        /**
         * Verifies that given calls occurred in specified order. It allows other calls in between.
         */
        public val order: VerifyMode get() = OrderVerifyMode

        /**
         * Verifies that given calls occurred in exact same order and there is no more calls.
         */
        public val exhaustiveOrder: VerifyMode = ExhaustiveOrderVerifyMode

        /**
         * Verifies that each function call occurred at least once in any order and there is no more calls.
         */
        public val exhaustive: VerifyMode = ExhaustiveSoftVerifyMode

        /**
         * Verifies that each function call occurred at least once in any order. It allows unverified calls.
         */
        public val soft: VerifyMode = SoftVerifyMode(atLeast = 1, atMost = Int.MAX_VALUE)

        /**
         * Verifies that each function call occurred at least [n] times. It allows unverified calls.
         */
        public fun atLeast(n: Int): VerifyMode = SoftVerifyMode(atLeast = n, atMost = Int.MAX_VALUE)

        /**
         * Verifies that each function call occurred at least once and at most [n] times. It allows unverified calls.
         */
        public fun atMost(n: Int): VerifyMode = SoftVerifyMode(atLeast = 1, atMost = n)

        /**
         * Verifies that each function call occurred exactly [n] times. It allows unverified calls.
         */
        public fun exactly(n: Int): VerifyMode = SoftVerifyMode(atLeast = n, atMost = n)

        /**
         * Verifies that number of calls for each function is within the given [range]. It allows unverified calls.
         */
        public fun inRange(range: IntRange): VerifyMode = SoftVerifyMode(atLeast = range.first, atMost = range.last)
    }
}

/**
 * Verify mode used by [VerifyMode.not].
 */
@InternalMokkeryApi
public object NotVerifyMode : VerifyMode()

/**
 * Verify mode used by [VerifyMode.order]
 */
@InternalMokkeryApi
public object OrderVerifyMode : VerifyMode()

/**
 * Verify mode used by [VerifyMode.exhaustiveOrder]
 */
@InternalMokkeryApi
public object ExhaustiveOrderVerifyMode : VerifyMode()

/**
 * Verify mode used by [VerifyMode.exhaustive]
 */
@InternalMokkeryApi
public object ExhaustiveSoftVerifyMode : VerifyMode()

/**
 * Verify mode used by [VerifyMode.soft], [VerifyMode.atLeast], [VerifyMode.atMost], [VerifyMode.exactly], [VerifyMode.inRange]
 */
@InternalMokkeryApi
public data class SoftVerifyMode(val atLeast: Int, val atMost: Int) : VerifyMode()

/**
 * Serializes and deserializes [VerifyMode] to pass it as compiler plugin option.
 */
@InternalMokkeryApi
public object VerifyModeSerializer {
    public fun serialize(verifyMode: VerifyMode): String = when (verifyMode) {
        ExhaustiveOrderVerifyMode -> "ExhaustiveOrder"
        ExhaustiveSoftVerifyMode -> "ExhaustiveSoft"
        NotVerifyMode -> "Not"
        OrderVerifyMode -> "Order"
        is SoftVerifyMode -> "Soft_${verifyMode.atLeast}_${verifyMode.atMost}"
    }

    public fun deserialize(value: String): VerifyMode = when {
        value == "ExhaustiveOrder" -> ExhaustiveOrderVerifyMode
        value == "ExhaustiveSoft" -> ExhaustiveSoftVerifyMode
        value == "Not" -> NotVerifyMode
        value == "Order" -> OrderVerifyMode
        value.startsWith("Soft") -> value.removePrefix("Soft_")
            .let { SoftVerifyMode(it.substringBefore("_").toInt(), it.substringAfter("_").toInt())}
        else -> error("Unknown verify mode!")
    }
}
