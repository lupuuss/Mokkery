package dev.mokkery.internal

internal sealed interface PropertyDescriptor {

    val name: String

    fun toCallString(args: List<String>): String

    data class Getter(override val name: String): PropertyDescriptor {

        override fun toCallString(args: List<String>) = when (args.size) {
            0 -> name
            else -> "(${args.joinToString()}).$name"
        }
    }

    data class Setter(override val name: String): PropertyDescriptor {

        override fun toCallString(args: List<String>): String {
            val arg = args.lastOrNull() ?: "<?>"
            return when {
                args.size > 1 -> "(${args.dropLast(1).joinToString()}).$name = $arg"
                else -> "$name = $arg"
            }
        }
    }

    companion object {

        fun fromNameOrNull(name: String) = when {
            !name.contains("-") || !name.endsWith(">") -> null
            name.startsWith("<get") -> Getter(name.substringAfter("-").substringBefore(">"))
            name.startsWith("<set") -> Setter(name.substringAfter("-").substringBefore(">"))
            else -> null
        }
    }
}