package dev.mokkery.internal.serialization.compiler.ast

internal fun interface SymbolTable {

    fun resolve(id: Identifier): List<Symbol>

    companion object {

        internal val standard = constSymbolTable(
            FunctionSymbol(
                name = Identifier.Range.value,
                type = Type.IntRange,
                parameters = listOf(
                    Parameter("a", Type.Int),
                    Parameter("b", Type.Int),
                ),
                body = { (a: Int, b: Int) -> a..b }
            ),
        )
    }
}

internal operator fun SymbolTable.plus(other: SymbolTable): SymbolTable {
    return SymbolTable { this.resolve(it) + other.resolve(it) }
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
