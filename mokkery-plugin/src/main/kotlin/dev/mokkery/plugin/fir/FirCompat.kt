package dev.mokkery.plugin.fir

import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType


fun FirClassLikeSymbol<*>.defaultTypeCompat(): ConeClassLikeType = try {
    this.defaultType()
} catch (e: NoSuchMethodError) {
    javaClass
        .classLoader
        .loadClass("org.jetbrains.kotlin.fir.resolve.ScopeUtilsKt")
        .methods
        .single { it.name == "defaultType" && it.parameters.firstOrNull()?.type?.simpleName == "FirClassSymbol" }
        .invoke(null, this) as ConeClassLikeType
}
