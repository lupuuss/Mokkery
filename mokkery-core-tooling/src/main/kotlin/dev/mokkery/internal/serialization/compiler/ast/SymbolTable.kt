package dev.mokkery.internal.serialization.compiler.ast

internal fun interface SymbolTable {

    fun resolve(id: Identifier): List<Symbol>
}

internal fun SymbolTable.resolveFunctions(id: Identifier) = resolve(id)
    .filterIsInstance<FunctionSymbol>()

internal fun SymbolTable.resolveProperty(id: Identifier) = resolve(id)
    .filterIsInstance<PropertySymbol>()

internal fun constSymbolTable(
    vararg symbols: Symbol,
): SymbolTable {
    val map = symbols.groupBy { it.name }
    return SymbolTable { id ->
        map[id.value].orEmpty()
    }
}
