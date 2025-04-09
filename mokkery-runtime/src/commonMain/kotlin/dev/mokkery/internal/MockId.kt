package dev.mokkery.internal

internal data class MockId(val typeName: String, val id: Long) {

    override fun toString(): String = "$typeName($id)"
}
