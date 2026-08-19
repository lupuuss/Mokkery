package dev.mokkery.plugin.ir.transformer.factory

import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.irFactory
import dev.mokkery.plugin.core.ir.pluginContext
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.addToCurrentFile
import dev.mokkery.plugin.core.ir.transformer.declarationIrBuilder
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.referencedDefaultType
import dev.mokkery.plugin.core.ir.transformer.replaceDeclarationIrBuilder
import dev.mokkery.plugin.ir.IrMokkeryKind
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.addOverridingMethod
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.erasedTypeArguments
import dev.mokkery.plugin.ir.findRegularParameters
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.requirePropertyGetterOwner
import dev.mokkery.plugin.ir.requireSimpleFunctionOwner
import dev.mokkery.plugin.ir.transformer.core.findOrBuildClassInCurrentFile
import dev.mokkery.plugin.ir.transformer.core.irGetMokkeryScopeFor
import dev.mokkery.plugin.ir.transformer.mock.buildMockClass
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.irAs
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.builders.irEquals
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irIfNull
import org.jetbrains.kotlin.ir.builders.irIfThen
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.isAny
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.createThisReceiverParameter
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.irConstructorCall
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.name.Name


context(scope: TransformerScope)
fun IrCall.replaceFactory(kind: IrMokkeryKind): IrExpression {
    val originalCall = this
    return replaceDeclarationIrBuilder {
        val function = originalCall.symbol.owner
        val regularParams = function.findRegularParameters()
        val classesParam = regularParams.first { it.isVararg }
        val blockParam = regularParams.first { !it.isVararg }
        val classes = (originalCall.arguments[classesParam] as? IrVararg)
            ?.elements
            .orEmpty()
            .map { (it as IrClassReference).classType.classOrFail.owner }
            .distinct()
            .sortedBy { it.kotlinFqName.asString() }
        val nameBase = when (kind) {
            IrMokkeryKind.Mock -> "MockFactory"
            IrMokkeryKind.Spy -> "SpyFactory"
        }
        val implClass = findOrBuildClassInCurrentFile(nameBase, classes) { buildFactory(it, kind, classes) }
        irCallConstructor(implClass.primaryConstructor!!) {
            arguments[0] = irGetMokkeryScopeFor(originalCall)
            arguments[1] = originalCall.arguments[blockParam] ?: irNull()
        }
    }
}

context(scope: TransformerScope)
private fun buildFactory(name: Name, kind: IrMokkeryKind, classes: List<IrClass>): IrClass {
    val factoryInterface = when (kind) {
        IrMokkeryKind.Mock -> referenced(MokkeryIr.Class.MockFactory)
        IrMokkeryKind.Spy -> referenced(MokkeryIr.Class.SpyFactory)
    }
    val mokkeryScopeClass = referenced(MokkeryIr.Class.MokkeryScope)
    val interceptedClasses = classes.map { cls ->
        findOrBuildClassInCurrentFile(
            nameBase = when (kind) {
                IrMokkeryKind.Mock -> "Mock"
                IrMokkeryKind.Spy -> "Spy"
            },
            nameHashSource = listOf(cls),
            builder = { buildMockClass(it, kind, cls) }
        )
    }
    val implClass = irFactory.buildClass {
        this.name = name
        this.origin = MokkeryIr.Origin
        this.visibility = DescriptorVisibilities.PRIVATE
        this.modality = Modality.FINAL
    }
    implClass.superTypes = listOf(irBuiltIns.anyType, factoryInterface.defaultType)
    implClass.createThisReceiverParameter()
    implClass.addToCurrentFile()
    val scopeField = buildField(implClass, "_scope", mokkeryScopeClass.defaultType)
    val constructor = implClass.addConstructor { isPrimary = true }.apply {
        val parentParam = addValueParameter("parent", mokkeryScopeClass.defaultType)
        val instanceFactoryConfigurerType = referencedDefaultType(MokkeryIr.Class.InstanceFactoryConfigurer)
        val blockType = irBuiltIns
            .functionN(1)
            .typeWith(instanceFactoryConfigurerType, irBuiltIns.unitType)
            .makeNullable()
        val blockParam = addValueParameter("block", blockType)
        body = symbol.declarationIrBuilder.irBlockBody {
            +irDelegatingConstructorCall(irBuiltIns.anyClass.owner.primaryConstructor!!)
            +irSetField(
                receiver = irGet(implClass.thisReceiver!!),
                field = scopeField,
                value = irCall(referenced(MokkeryIr.Function.instanceFactoryScope)) {
                    arguments[0] = irGet(parentParam)
                    arguments[1] = irGet(blockParam)
                }
            )
        }
    }
    val createOrNullFun = factoryInterface.requireSimpleFunctionOwner("createOrNull")
    implClass.addOverridingMethod(pluginContext, createOrNullFun) { func ->
        val thisParam = func.parameters[0]
        val typeParam = func.parameters[1]
        val spyParam = func.parameters.find { it.type.isAny() }
        val blockParam = func.parameters.last()
        val classifierGetter = irBuiltIns.kTypeClass.owner.requirePropertyGetterOwner("classifier")
        val classifier = irTemporary(irCall(classifierGetter) { arguments[0] = irGet(typeParam) })
        classes.zip(interceptedClasses).forEach { (cls, interceptedCls) ->
            val constructorCall = irInterceptedConstructorCall(
                kind = kind,
                cls = cls,
                interceptedCls = interceptedCls,
                scopeExpression = irGetField(irGet(thisParam), scopeField),
                typeExpression = { irGet(typeParam) },
                spyExpression = spyParam?.let(::irGet),
                blockExpression = irGet(blockParam)
            )
            +irIfThen(
                type = irBuiltIns.unitType,
                condition = irEquals(irGet(classifier), kClassReference(cls.defaultTypeErased)),
                thenPart = irReturn(constructorCall)
            )
        }
        +irReturn(irNull())
    }
    val copyFun = factoryInterface.requireSimpleFunctionOwner("copy")
    implClass.addOverridingMethod(pluginContext, copyFun) {
        val thisParam = it.parameters[0]
        val newScopeParam = it.parameters[1]
        val blockParam = it.parameters[2]
        +irReturn(
            irCallConstructor(constructor) {
                arguments[0] = irIfNull(
                    type = mokkeryScopeClass.defaultType,
                    subject = irGet(newScopeParam),
                    thenPart = irGetField(irGet(thisParam), scopeField),
                    elsePart = irAs(irGet(newScopeParam), mokkeryScopeClass.defaultType)
                )
                arguments[1] = irGet(blockParam)
            }
        )
    }
    return implClass
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.irInterceptedConstructorCall(
    kind: IrMokkeryKind,
    cls: IrClass,
    interceptedCls: IrClass,
    scopeExpression: IrExpression,
    spyExpression: IrExpression?,
    blockExpression: IrExpression,
    typeExpression: () -> IrExpression
): IrConstructorCall = irCallConstructor(interceptedCls.primaryConstructor!!, interceptedCls.erasedTypeArguments) {
    arguments[0] = scopeExpression
    val typeArgsOffset = when (kind) {
        IrMokkeryKind.Mock -> {
            arguments[1] = irNull()
            arguments[2] = blockExpression
            3
        }
        IrMokkeryKind.Spy -> {
            arguments[1] = irNull()
            arguments[2] = blockExpression
            arguments[3] = irAs(spyExpression!!, cls.defaultTypeErased)
            4
        }
    }
    repeat(interceptedCls.typeParameters.size) { index ->
        arguments[typeArgsOffset + index] = buildKTypeArgumentAtCall(typeExpression(), index)
    }
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.buildKTypeArgumentAtCall(
    typeExpression: IrExpression,
    index: Int
): IrExpression = irCall(referenced(MokkeryIr.Function.getTypeArgumentClassOrAny)) {
    arguments[0] = typeExpression
    arguments[1] = irInt(index)
}

context(scope: TransformerScope)
private fun buildField(
    cls: IrClass,
    name: String,
    type: IrType
): IrField = irFactory.buildField {
    this.name = Name.identifier(name)
    this.type = type
    this.visibility = DescriptorVisibilities.PRIVATE
    this.isFinal = true
    this.origin = MokkeryIr.Origin
}.also {
    it.parent = cls
    cls.declarations.add(it)
}
