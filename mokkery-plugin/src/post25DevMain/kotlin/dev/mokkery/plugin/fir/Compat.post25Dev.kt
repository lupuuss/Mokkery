package dev.mokkery.plugin.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.isArrayOfCall
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall

actual fun FirFunctionCall.isArrayOfCallCompat(session: FirSession): Boolean = isArrayOfCall()
