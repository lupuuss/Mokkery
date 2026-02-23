package dev.mokkery.plugin.fir.compat

import org.jetbrains.kotlin.descriptors.runtime.components.tryLoadClass
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.FirFunction

fun FirFunction.isNamedFunctionCompat(): Boolean {
    if (firNamedFunctionClass != null) return firNamedFunctionClass.isInstance(this)
    return requireNotNull(firSimpleFunctionClass) { "Incompatible Kotlin compiler version detected!" }.isInstance(this)
}

fun FirElement.isSimpleFunctionCompat(): Boolean = firSimpleFunctionClass?.isInstance(this) ?: false

private val firNamedFunctionClass = Unit::class.java
    .classLoader
    .tryLoadClass("org.jetbrains.kotlin.fir.declarations.FirNamedFunction")

private val firSimpleFunctionClass = Unit::class.java
    .classLoader
    .tryLoadClass("org.jetbrains.kotlin.fir.declarations.FirSimpleFunction")
