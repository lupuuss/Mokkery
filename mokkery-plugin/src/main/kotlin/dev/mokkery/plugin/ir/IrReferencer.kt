package dev.mokkery.plugin.ir

import dev.mokkery.plugin.ir.compat.referenceClassCompat
import dev.mokkery.plugin.ir.compat.referenceFunctionsCompat
import dev.mokkery.plugin.ir.compat.referencePropertiesCompat
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
    override fun reference(context: IrPluginContext): IrSimpleFunction = context.referenceFunctionsCompat(id)
        .find(predicate)
        ?.owner
        ?: runtimeDependencyError(id.toString())
}

class IrClassById(private val id: ClassId) : IrClassReferencer {
    override fun reference(context: IrPluginContext): IrClass = context.referenceClassCompat(id)
        ?.owner
        ?: runtimeDependencyError(id.toString())
}

class IrPropertyById(private val id: CallableId) : IrPropertyReferencer {
    override fun reference(context: IrPluginContext): IrProperty = context.referencePropertiesCompat(id)
        .firstOrNull()
        ?.owner
        ?: runtimeDependencyError(id.toString())
}

private fun runtimeDependencyError(id: String): Nothing = error("Declaration $id could not be found!")
