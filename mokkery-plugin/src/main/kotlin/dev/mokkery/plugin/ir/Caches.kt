package dev.mokkery.plugin.ir

import dev.mokkery.plugin.cacheKey
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

object Caches {

    val classReferences by cacheKey<IrClassReferencer, IrClass>()
    val functionReferences by cacheKey<IrFunctionReferencer, IrSimpleFunction>()
    val propertyReferences by cacheKey<IrPropertyReferencer, IrProperty>()


    val mockClasses by cacheKey<IrClass, IrClass>()
    val spyClasses by cacheKey<IrClass, IrClass>()
    val mockManyClasses by cacheKey<Set<IrClass>, IrClass>()
}
