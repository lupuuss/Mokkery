package dev.mokkery.internal.serialization

import dev.mokkery.verify.ExhaustiveOrderVerifyMode
import dev.mokkery.verify.ExhaustiveSoftVerifyMode
import dev.mokkery.verify.NotVerifyMode
import dev.mokkery.verify.OrderVerifyMode
import dev.mokkery.verify.SoftVerifyMode
import dev.mokkery.verify.VerifyMode

internal object VerifyModeSerializer : MokkerySerializer<VerifyMode> {

    override fun serialize(obj: VerifyMode): String = when (obj) {
        ExhaustiveOrderVerifyMode -> "ExhaustiveOrder"
        ExhaustiveSoftVerifyMode -> "ExhaustiveSoft"
        NotVerifyMode -> "Not"
        OrderVerifyMode -> "Order"
        is SoftVerifyMode -> "Soft_${obj.atLeast}_${obj.atMost}"
    }

    override fun deserialize(string: String): VerifyMode = when {
        string == "ExhaustiveOrder" -> ExhaustiveOrderVerifyMode
        string == "ExhaustiveSoft" -> ExhaustiveSoftVerifyMode
        string == "Not" -> NotVerifyMode
        string == "Order" -> OrderVerifyMode
        string.startsWith("Soft") -> string.removePrefix("Soft_")
            .let { SoftVerifyMode(it.substringBefore("_").toInt(), it.substringAfter("_").toInt())}
        else -> error("Unknown verify mode!")
    }
}
