package dev.mokkery.plugin.core.ir

import dev.mokkery.plugin.core.cacheKey
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

object Caches {

    val classReferences by cacheKey<IrClassReferencer, IrClass>()
    val functionReferences by cacheKey<IrFunctionReferencer, IrSimpleFunction>()
    val propertyReferences by cacheKey<IrPropertyReferencer, IrProperty>()
}
