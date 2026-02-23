package dev.mokkery.plugin.ir

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
    override fun reference(context: IrPluginContext): IrSimpleFunction = context.referenceFunctions(id).first(predicate).owner
}

class IrClassById(private val id: ClassId) : IrClassReferencer {
    override fun reference(context: IrPluginContext): IrClass = context.referenceClass(id)!!.owner
}

class IrPropertyById(private val id: CallableId) : IrPropertyReferencer {
    override fun reference(context: IrPluginContext): IrProperty = context.referenceProperties(id).first().owner
}
