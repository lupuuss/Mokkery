package dev.mokkery.plugin.core

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId

interface TransformerScope : CompilerPluginScope {

    val currentFile: IrFile

    val classes: MutableMap<ClassResolver, IrClass>
    val functions: MutableMap<FunctionResolver, IrSimpleFunction>
    val properties: MutableMap<PropertyResolver, IrProperty>
}

interface ClassResolver {
    fun resolve(context: IrPluginContext): IrClass
}

interface FunctionResolver {
    fun resolve(context: IrPluginContext): IrSimpleFunction
}

interface PropertyResolver {
    fun resolve(context: IrPluginContext): IrProperty
}

class FunctionById(private val id: CallableId) : FunctionResolver {
    override fun resolve(context: IrPluginContext): IrSimpleFunction = context.referenceFunctions(id).first().owner
}

class ClassById(private val id: ClassId) : ClassResolver {
    override fun resolve(context: IrPluginContext): IrClass = context.referenceClass(id)!!.owner
}

class PropertyById(private val id: CallableId) : PropertyResolver {
    override fun resolve(context: IrPluginContext): IrProperty = context.referenceProperties(id).first().owner
}
