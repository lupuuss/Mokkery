package dev.mokkery.plugin.core.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.constructors
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol

fun FirClassSymbol<*>.hasMokkeryGeneratedConstructor(
    session: FirSession
): Boolean = constructors(session).any(FirConstructorSymbol::isMokkeryGeneratedConstructor)
