package dev.mokkery.internal

internal data class MokkeryInstanceId(val typeName: String, val id: Long) {

    override fun toString(): String = "$typeName($id)"
}
