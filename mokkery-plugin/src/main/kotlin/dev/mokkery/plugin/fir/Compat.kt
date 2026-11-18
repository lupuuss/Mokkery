package dev.mokkery.plugin.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall

expect fun FirFunctionCall.isArrayOfCallCompat(session: FirSession): Boolean
