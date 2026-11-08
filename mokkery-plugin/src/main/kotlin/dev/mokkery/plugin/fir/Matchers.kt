package dev.mokkery.plugin.fir

import dev.mokkery.plugin.Mokkery
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.isMarkedNullable

val contextFunctionIds = setOf(Mokkery.Callable.ctx, Mokkery.Callable.ext)

fun FirFunctionSymbol<*>.signatureParameters(): List<Pair<FirBasedSymbol<*>, ConeKotlinType>> = buildList {
    receiverParameterSymbol?.let { add(it to it.resolvedType) }
    contextParameterSymbols.forEach { add(it to it.resolvedReturnType) }
    valueParameterSymbols.forEach { add(it to it.resolvedReturnType) }
}

fun FirFunctionSymbol<*>.hasMatcherScope(): Boolean = hasScope { it.isMokkeryMatcherScope() && !it.isMarkedNullable }

fun FirFunctionSymbol<*>.hasTemplatingScope(): Boolean = hasScope { it.isMokkeryTemplatingScope() }

private inline fun FirFunctionSymbol<*>.hasScope(isScope: (ConeKotlinType) -> Boolean): Boolean {
    return dispatchReceiverType?.let(isScope) == true || signatureParameters().any { isScope(it.second) }
}

fun FirBasedSymbol<*>.acceptsMatcher(session: FirSession): Boolean = hasAnnotation(matcherAnnotation, session)

fun FirBasedSymbol<*>.getMatcherAnnotation(session: FirSession): FirAnnotation? = getAnnotationByClassId(matcherAnnotation, session)

fun ConeKotlinType?.isMokkeryMatcherScope(): Boolean = this?.classId == mokkeryMatcherScopeClassId

fun ConeKotlinType?.isMokkeryTemplatingScope(): Boolean = this?.classId == mokkeryTemplatingScopeClassId

private val matcherAnnotation = Mokkery.ClassId.Matcher
private val mokkeryMatcherScopeClassId = Mokkery.ClassId.MokkeryMatcherScope
private val mokkeryTemplatingScopeClassId = Mokkery.ClassId.MokkeryTemplatingScope
