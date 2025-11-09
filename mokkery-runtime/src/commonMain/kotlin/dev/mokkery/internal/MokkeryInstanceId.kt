package dev.mokkery.internal

internal class MokkeryInstanceId(val typeName: String, val id: Long) {

    override fun toString(): String = "$typeName($id)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as MokkeryInstanceId
        if (id != other.id) return false
        if (typeName != other.typeName) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + typeName.hashCode()
        return result
    }

    fun copy(typeName: String = this.typeName, id: Long = this.id) = MokkeryInstanceId(typeName, id)
}
