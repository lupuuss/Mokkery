package dev.mokkery.internal.options

import dev.mokkery.MockMode
import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.internal.serialization.BooleanSerializer
import dev.mokkery.internal.serialization.MokkerySerializer
import dev.mokkery.internal.serialization.VerifyModeSerializer
import dev.mokkery.internal.serialization.enumSerializer
import dev.mokkery.verify.VerifyMode
import kotlin.enums.enumEntries

@InternalMokkeryApi
public data class MokkeryOptionType<T>(
    public val description: String,
    public val serializer: MokkerySerializer<T & Any>,
) {

    public companion object {

        public val boolean: MokkeryOptionType<Boolean> = MokkeryOptionType(
            description = "<true|false>",
            serializer = BooleanSerializer
        )

        public val verifyMode: MokkeryOptionType<VerifyMode> = MokkeryOptionType(
            description = $$"<ExhaustiveOrder|ExhaustiveSoft|Not|Order|Soft_$atLeast_$atMost>",
            serializer = VerifyModeSerializer
        )

        public val mockMode: MokkeryOptionType<MockMode> = enum<MockMode>()

        internal inline fun <reified T : Enum<T>> enum(): MokkeryOptionType<T> = MokkeryOptionType(
            description = enumEntries<T>().joinToString(separator = "|", prefix = "<", postfix = ">"),
            serializer = enumSerializer<T>()
        )
    }
}



