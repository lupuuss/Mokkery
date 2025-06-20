package dev.mokkery.plugin.fir

import dev.mokkery.plugin.core.Mokkery
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.classId

fun FirFunctionCall.isMatcher(): Boolean {
    val callee = calleeReference as? FirResolvedNamedReference ?: return false
    val symbol = callee.resolvedSymbol as? FirNamedFunctionSymbol ?: return false
    return symbol.isMatcher()
}

fun FirFunctionSymbol<*>.isMatcher(): Boolean {
    return receiverParameterSymbol?.resolvedType.isArgMatchersScope()
            || dispatchReceiverType.isArgMatchersScope()
            || valueParameterSymbols.any { it.resolvedReturnType.isArgMatchersScope() }
            || contextParameterSymbols.any { it.resolvedReturnType.isArgMatchersScope() }
}


fun FirFunctionSymbol<*>.isVarargMatcher(session: FirSession): Boolean {
    return isMatcher() && hasAnnotation(varArgMacherBuilderAnnotation, session)
}

fun FirFunctionSymbol<*>.isCompositeMatcher(session: FirSession): Boolean {
    return isMatcher() && contextParameterSymbols
        .plus(valueParameterSymbols)
        .any { it.hasAnnotation(matcherAnnotation, session) }
}

fun FirBasedSymbol<*>.acceptsMatcher(session: FirSession): Boolean = hasAnnotation(matcherAnnotation, session)

@Suppress("NOTHING_TO_INLINE")
private inline fun ConeKotlinType?.isArgMatchersScope(): Boolean = this?.classId == argMatchersScopeClassId

private val matcherAnnotation = Mokkery.ClassId.Matcher
private val varArgMacherBuilderAnnotation = Mokkery.ClassId.VarArgMatcherBuilder
private val argMatchersScopeClassId = Mokkery.ClassId.ArgMatchersScope
