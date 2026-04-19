package dev.mokkery.plugin.core.fir

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.constructors
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol

fun FirClassSymbol<*>.hasMokkeryGeneratedConstructor(
    session: FirSession
): Boolean = constructors(session).any(FirConstructorSymbol::isMokkeryGeneratedConstructor)

fun FirClassSymbol<*>.superClassSymbolOrNull(
    session: FirSession
): FirRegularClassSymbol? = resolvedSuperTypes.firstNotNullOfOrNull { superType ->
    superType.toRegularClassSymbol(session)?.takeIf { it.classKind == ClassKind.CLASS }
}
