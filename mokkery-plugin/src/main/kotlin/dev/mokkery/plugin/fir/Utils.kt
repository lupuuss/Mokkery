package dev.mokkery.plugin.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.declaredFunctions
import org.jetbrains.kotlin.fir.declarations.declaredProperties
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol

fun FirRegularClassSymbol.declaredMembers(session: FirSession): List<FirBasedSymbol<*>> {
    return declaredFunctions(session) + declaredProperties(session)
}
