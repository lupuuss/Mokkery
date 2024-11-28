package dev.mokkery.plugin.fir

import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol

val FirRegularClassSymbol.constructors get() = declarationSymbols.filterIsInstance<FirConstructorSymbol>()
