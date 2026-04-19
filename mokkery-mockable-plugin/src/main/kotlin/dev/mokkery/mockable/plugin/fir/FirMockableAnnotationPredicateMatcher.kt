package dev.mokkery.mockable.plugin.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.extensions.utils.AbstractSimpleClassPredicateMatchingService
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.name.FqName

fun FirSession.isMockableAnnotated(declaration: FirRegularClass): Boolean {
    return mockableAnnotationPredicateMatcher.isAnnotated(declaration.symbol)
}

fun FirSession.isMockableAnnotated(symbol: FirRegularClassSymbol): Boolean {
    return mockableAnnotationPredicateMatcher.isAnnotated(symbol)
}

val FirSession.mockableAnnotationPredicateMatcher: FirMockableAnnotationPredicateMatcher by FirSession.sessionComponentAccessor()

class FirMockableAnnotationPredicateMatcher(
    session: FirSession,
    annotations: List<FqName>
) : AbstractSimpleClassPredicateMatchingService(session) {

    override val predicate = DeclarationPredicate.create {
        annotated(annotations) or metaAnnotated(annotations, includeItself = true)
    }
}
