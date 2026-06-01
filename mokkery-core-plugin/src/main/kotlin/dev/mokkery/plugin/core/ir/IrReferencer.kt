package dev.mokkery.plugin.core.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId

interface IrClassReferencer {
    fun reference(context: IrPluginContext): IrClass
}

interface IrFunctionReferencer {
    fun reference(context: IrPluginContext): IrSimpleFunction
}

interface IrPropertyReferencer {
    fun reference(context: IrPluginContext): IrProperty
}

class IrFunctionById(
    private val id: CallableId,
    private val predicate: (IrSimpleFunctionSymbol) -> Boolean = { true }
) : IrFunctionReferencer {


    override fun reference(context: IrPluginContext): IrSimpleFunction = context.finderForBuiltins()
        .findFunctions(id)
        .find(predicate)
        ?.owner
        ?: runtimeDependencyError(id.toString())
}

class IrClassById(private val id: ClassId) : IrClassReferencer {
    override fun reference(context: IrPluginContext): IrClass = context.finderForBuiltins()
        .findClass(id)
        ?.owner
        ?: runtimeDependencyError(id.toString())
}

class IrPropertyById(private val id: CallableId) : IrPropertyReferencer {
    override fun reference(context: IrPluginContext): IrProperty = context.finderForBuiltins()
        .findProperties(id)
        .firstOrNull()
        ?.owner
        ?: runtimeDependencyError(id.toString())
}

private fun runtimeDependencyError(id: String): Nothing = error("Declaration $id could not be found!")
