package dev.mokkery.plugin.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.resolve.isArrayOfCall

actual fun FirFunctionCall.isArrayOfCallCompat(session: FirSession): Boolean = isArrayOfCall(session)
