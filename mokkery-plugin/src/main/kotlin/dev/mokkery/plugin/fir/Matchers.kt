package dev.mokkery.plugin.fir

import dev.mokkery.plugin.core.Mokkery
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.classId

fun FirFunctionSymbol<*>.isMatcher(): Boolean {
    return receiverParameterSymbol?.resolvedType.isMokkeryMatcherScope()
            || dispatchReceiverType.isMokkeryMatcherScope()
            || valueParameterSymbols.any { it.resolvedReturnType.isMokkeryMatcherScope() }
            || contextParameterSymbols.any { it.resolvedReturnType.isMokkeryMatcherScope() }
}

fun FirFunctionSymbol<*>.isTemplatingFunction(): Boolean {
    return receiverParameterSymbol?.resolvedType.isMokkeryTemplatingScope()
            || dispatchReceiverType.isMokkeryTemplatingScope()
            || valueParameterSymbols.any { it.resolvedReturnType.isMokkeryTemplatingScope() }
            || contextParameterSymbols.any { it.resolvedReturnType.isMokkeryTemplatingScope() }
}

fun FirFunctionSymbol<*>.isCompositeMatcher(session: FirSession): Boolean {
    return isMatcher() && contextParameterSymbols
        .plus(valueParameterSymbols)
        .any { it.hasAnnotation(matcherAnnotation, session) }
}

fun FirBasedSymbol<*>.acceptsMatcher(session: FirSession): Boolean = hasAnnotation(matcherAnnotation, session)

fun FirBasedSymbol<*>.getMatcherAnnotation(session: FirSession): FirAnnotation? = getAnnotationByClassId(matcherAnnotation, session)

@Suppress("NOTHING_TO_INLINE")
fun ConeKotlinType?.isMokkeryMatcherScope(): Boolean = this?.classId == mokkeryMatcherScopeClassId

fun ConeKotlinType?.isMokkeryTemplatingScope(): Boolean = this?.classId == mokkeryTemplatingScopeClassId

private val matcherAnnotation = Mokkery.ClassId.Matcher
private val mokkeryMatcherScopeClassId = Mokkery.ClassId.MokkeryMatcherScope
private val mokkeryTemplatingScopeClassId = Mokkery.ClassId.MokkeryTemplatingScope
