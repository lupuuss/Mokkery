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
    return receiverParameterSymbol?.resolvedType.isArgMatchersScope()
            || dispatchReceiverType.isArgMatchersScope()
            || valueParameterSymbols.any { it.resolvedReturnType.isArgMatchersScope() }
            || contextParameterSymbols.any { it.resolvedReturnType.isArgMatchersScope() }
}

fun FirFunctionSymbol<*>.isTemplatingFunction(): Boolean {
    return receiverParameterSymbol?.resolvedType.isTemplatingScope()
            || dispatchReceiverType.isTemplatingScope()
            || valueParameterSymbols.any { it.resolvedReturnType.isTemplatingScope() }
            || contextParameterSymbols.any { it.resolvedReturnType.isTemplatingScope() }
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

fun FirBasedSymbol<*>.getMatcherAnnotation(session: FirSession): FirAnnotation? = getAnnotationByClassId(matcherAnnotation, session)

@Suppress("NOTHING_TO_INLINE")
fun ConeKotlinType?.isArgMatchersScope(): Boolean = this?.classId == argMatchersScopeClassId

fun ConeKotlinType?.isTemplatingScope(): Boolean = this?.classId == templatingScopeClassId

private val matcherAnnotation = Mokkery.ClassId.Matcher
private val varArgMacherBuilderAnnotation = Mokkery.ClassId.VarArgMatcherBuilder
private val argMatchersScopeClassId = Mokkery.ClassId.ArgMatchersScope
private val templatingScopeClassId = Mokkery.ClassId.TemplatingScope
