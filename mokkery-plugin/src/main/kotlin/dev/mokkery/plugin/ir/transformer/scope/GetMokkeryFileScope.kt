package dev.mokkery.plugin.ir.transformer.file

import dev.mokkery.plugin.core.context.configuration
import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.irFactory
import dev.mokkery.plugin.core.ir.pluginContext
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.addToCurrentFile
import dev.mokkery.plugin.core.ir.transformer.declarationIrBuilder
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.defaultMockMode
import dev.mokkery.plugin.defaultVerifyMode
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.addOverridingMethod
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irGetEnumEntry
import dev.mokkery.plugin.ir.overridePropertyBackingField
import dev.mokkery.plugin.ir.requirePropertyOwner
import dev.mokkery.plugin.ir.requireSimpleFunctionOwner
import dev.mokkery.plugin.ir.transformer.core.findOrBuildClassInCurrentFile
import dev.mokkery.plugin.ir.transformer.core.irGetMokkeryScopeGlobal
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verify.VerifyModeInternals.Soft
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irConcat
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.addArgument
import org.jetbrains.kotlin.ir.util.createThisReceiverParameter
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.nestedClasses
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.name.Name

context(scope: TransformerScope)
fun IrBuilderWithScope.irGetMokkeryFileScope(): IrExpression {
    val fileScopeObj = findOrBuildClassInCurrentFile("MokkeryFileScope") {
        buildFileScopeClass(it)
    }
    return irGetObject(fileScopeObj.symbol)
}

context(scope: TransformerScope)
private fun buildFileScopeClass(name: Name): IrClass {
    val createFileContextFun = referenced(MokkeryIr.Function.createFileContext)
    val mockModeClass = referenced(MokkeryIr.Class.MockMode)
    val cls = irFactory.buildClass {
        this.name = name
        this.kind = ClassKind.OBJECT
        this.visibility = DescriptorVisibilities.PRIVATE
        this.origin = MokkeryIr.Origin
    }
    cls.addToCurrentFile()
    cls.createThisReceiverParameter()
    val scopeClass = referenced(MokkeryIr.Class.MokkeryScope)
    cls.superTypes = listOf(irBuiltIns.anyType, scopeClass.defaultType)
    val contextProperty = cls.overridePropertyBackingField(
        context = pluginContext,
        property = scopeClass.requirePropertyOwner("mokkeryContext")
    )
    val toStringFun = irBuiltIns.anyClass.owner.requireSimpleFunctionOwner("toString")
    cls.addOverridingMethod(pluginContext, toStringFun) {
        val concat = irConcat()
        concat.addArgument(irString("MokkeryScope(mokkeryContext="))
        concat.addArgument(
            irCall(toStringFun) {
                arguments[0] = irGetField(irGet(it.parameters[0]), contextProperty.backingField!!)
            }
        )
        concat.addArgument(irString(")"))
        +irReturn(concat)
    }
    cls.addConstructor {
        isPrimary = true
        origin = MokkeryIr.Origin
    }.apply {
        body = symbol.declarationIrBuilder.irBlockBody {
            +irDelegatingConstructorCall(irBuiltIns.anyClass.owner.primaryConstructor!!)
            +irSetField(
                receiver = irGet(cls.thisReceiver!!),
                field = contextProperty.backingField!!,
                value = irCall(createFileContextFun) {
                    arguments[0] = irGetMokkeryScopeGlobal()
                    arguments[1] = irGetEnumEntry(mockModeClass, configuration.defaultMockMode)
                    arguments[2] = irGetVerifyMode(configuration.defaultVerifyMode)
                }
            )
        }
    }
    return cls
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.irGetVerifyMode(verifyMode: VerifyMode) = when (verifyMode) {
    is Soft -> irCallConstructor(verifyMode.toIrClass().primaryConstructor!!) {
        arguments[0] = irInt(verifyMode.atLeast)
        arguments[1] = irInt(verifyMode.atMost)
    }
    else -> irGetObject(verifyMode.toIrClass().symbol)
}

context(scope: TransformerScope)
private fun VerifyMode.toIrClass(): IrClass {
    val simpleName = this::class.simpleName
    return referenced(MokkeryIr.Class.VerifyModeInternals)
        .nestedClasses
        .find { it.name.asString() == simpleName }!!
}
