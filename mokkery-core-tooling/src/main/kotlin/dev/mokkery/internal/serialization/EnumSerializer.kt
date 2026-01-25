package dev.mokkery.internal.serialization

internal inline fun <reified T : Enum<T>> enumSerializer(): MokkerySerializer<T> {
    return object : MokkerySerializer<T> {
        override fun serialize(obj: T): String = obj.name

        override fun deserialize(string: String): T = enumValueOf<T>(string)
    }
}
