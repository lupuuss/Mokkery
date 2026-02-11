package dev.mokkery.internal.serialization

internal object BooleanSerializer : MokkerySerializer<Boolean> {

    override fun serialize(obj: Boolean): String = obj.toString()

    override fun deserialize(string: String): Boolean = string.toBooleanStrict()
}
