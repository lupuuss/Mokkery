package dev.mokkery.test

import dev.mokkery.context.Function
import dev.mokkery.internal.context.MemberFunctions

internal class TestMemberFunctions(
    private val functions: List<Function> = emptyList(),
) : MemberFunctions {

    var lookups = 0
        private set

    override fun getOrNull(id: Function.Id): Function? {
        lookups++
        return functions.find { it.id == id }
    }

    override fun normalizeId(id: Function.Id): Function.Id = id

    override fun get(id: Function.Id): Function = getOrNull(id) ?: error("No function registered for id ${id.value}! Registered functions: ${functions.map(Function::name)}")
}
