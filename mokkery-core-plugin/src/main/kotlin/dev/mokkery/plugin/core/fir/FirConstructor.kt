package dev.mokkery.plugin.core.fir

import dev.mokkery.plugin.core.MokkeryCore
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.types.isUnit

fun FirConstructorSymbol.isMokkeryGeneratedConstructor(): Boolean {
    val param = valueParameterSymbols.firstOrNull() ?: return false
    return param.name == MokkeryCore.Names.mockableConstructorMarkerParam
            && param.resolvedReturnType.isUnit
}
