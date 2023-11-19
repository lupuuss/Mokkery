package dev.mokkery.plugin.fir

import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol


fun FirConstructorSymbol.isDefault() = valueParameterSymbols.isEmpty() || valueParameterSymbols.all { it.hasDefaultValue }

val FirRegularClassSymbol.constructors get() = declarationSymbols.filterIsInstance<FirConstructorSymbol>()
