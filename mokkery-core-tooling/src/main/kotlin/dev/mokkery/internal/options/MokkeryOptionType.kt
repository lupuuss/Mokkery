package dev.mokkery.internal.options

import dev.mokkery.MockMode
import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.internal.serialization.AnnotationSelectorSerializer
import dev.mokkery.internal.serialization.BooleanSerializer
import dev.mokkery.internal.serialization.MokkerySerializer
import dev.mokkery.internal.serialization.VerifyModeSerializer
import dev.mokkery.internal.serialization.enumSerializer
import dev.mokkery.options.AnnotationSelector
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
            description = $$"VerifyMode expression <exhaustive|order|exhaustiveOrder|not|soft|atLeast($n)|atMost($n)|inRange($range)|exactly($n)>",
            serializer = VerifyModeSerializer
        )

        public val mockMode: MokkeryOptionType<MockMode> = enum<MockMode>()

        public val annotationSelector: MokkeryOptionType<AnnotationSelector> = MokkeryOptionType(
            description = $$"AnnotationSelector expression <all|none|named($names)|matches($pattern|$options)|+|->",
            serializer = AnnotationSelectorSerializer
        )

        internal inline fun <reified T : Enum<T>> enum(): MokkeryOptionType<T> = MokkeryOptionType(
            description = "enum ${T::class.simpleName}" + enumEntries<T>().joinToString(separator = "|", prefix = "<", postfix = ">"),
            serializer = enumSerializer<T>()
        )
    }
}



