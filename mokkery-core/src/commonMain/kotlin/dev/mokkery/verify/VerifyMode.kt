package dev.mokkery.verify

import dev.mokkery.annotations.InternalMokkeryApi

public sealed class VerifyMode {

    public companion object {

        public val not: VerifyMode get() = NotVerifyMode

        public val order: VerifyMode get() = OrderVerifyMode

        public val exhaustiveOrder: VerifyMode = ExhaustiveOrderVerifyMode

        public val exhaustive: VerifyMode = ExhaustiveSoftVerifyMode

        public val soft: VerifyMode = SoftVerifyMode(atLeast = 1, atMost = Int.MAX_VALUE)

        public fun atLeast(value: Int): VerifyMode = SoftVerifyMode(atLeast = value, atMost = Int.MAX_VALUE)

        public fun atMost(value: Int): VerifyMode = SoftVerifyMode(atLeast = 1, atMost = value)

        public fun exactly(value: Int): VerifyMode = SoftVerifyMode(atLeast = value, atMost = value)

        public fun inRange(range: IntRange): VerifyMode = SoftVerifyMode(atLeast = range.first, atMost = range.last)
    }
}

@InternalMokkeryApi
public object NotVerifyMode : VerifyMode()

@InternalMokkeryApi
public object OrderVerifyMode : VerifyMode()

@InternalMokkeryApi
public object ExhaustiveOrderVerifyMode : VerifyMode()

@InternalMokkeryApi
public object ExhaustiveSoftVerifyMode : VerifyMode()

@InternalMokkeryApi
public data class SoftVerifyMode(val atLeast: Int, val atMost: Int) : VerifyMode()
