package dev.mokkery.plugin.ir.transformer.module

import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.core.cacheKey
import dev.mokkery.plugin.core.caches
import dev.mokkery.plugin.core.context.configuration
import dev.mokkery.plugin.core.getOrPut
import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.irFactory
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.addToCurrentFile
import dev.mokkery.plugin.core.ir.transformer.currentFileValue
import dev.mokkery.plugin.core.ir.transformer.declarationIrBuilder
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.referencedCompanion
import dev.mokkery.plugin.defaultMockMode
import dev.mokkery.plugin.defaultVerifyMode
import dev.mokkery.plugin.ir.KotlinIr
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irGetEnumEntry
import dev.mokkery.plugin.ir.irLambdaOf
import dev.mokkery.plugin.ir.requirePropertyGetterOwner
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verify.VerifyModeInternals.Soft
import org.jetbrains.kotlin.config.moduleName
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.callableId
import org.jetbrains.kotlin.ir.util.findDeclaration
import org.jetbrains.kotlin.ir.util.nestedClasses
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.name.Name

private val moduleScopeAccessorCache by cacheKey<Unit, IrSimpleFunction>()

context(scope: TransformerScope)
val moduleScopePropertyAccessor: IrSimpleFunction
    get() = caches[moduleScopeAccessorCache].getOrPut(Unit) {
        currentFileValue
            .module
            .files
            .firstNotNullOfOrNull { it.findDeclaration<IrProperty>(IrProperty::isModuleScope) }
            ?.getter
            ?: error("Declaration ${Mokkery.Callable.module} could not be found!")
    }

private val IrProperty.isModuleScope: Boolean
    get() = origin == MokkeryIr.Origin && callableId == Mokkery.Callable.module

context(scope: TransformerScope)
fun IrSimpleFunction.generateBodyIfModuleScopeGetter() {
    if (correspondingPropertySymbol?.owner?.isModuleScope != true) return
    val scopeType = returnType
    body = symbol.declarationIrBuilder {
        val field = buildModuleScopeFieldInCurrentFile(scopeType)
        val valueGetter = referenced(KotlinIr.Class.Lazy).requirePropertyGetterOwner("value")
        irBlockBody {
            +irReturn(irCall(valueGetter, scopeType) { arguments[0] = irGetField(null, field) })
        }
    }
}

context(scope: TransformerScope)
private fun buildModuleScopeFieldInCurrentFile(scopeType: IrType): IrField {
    val field = irFactory.buildField {
        name = Name.identifier("_mokkeryModuleScope")
        visibility = DescriptorVisibilities.PRIVATE
        isFinal = true
        isStatic = true
        origin = MokkeryIr.Origin
        type = referenced(KotlinIr.Class.Lazy).typeWith(scopeType)
    }
    field.addToCurrentFile()
    field.initializer = field.symbol.declarationIrBuilder {
        irExprBody(
            irCall(referenced(KotlinIr.Function.lazy)) {
                typeArguments[0] = scopeType
                arguments[0] = irLambdaOf(irBuiltIns.functionN(0).typeWith(scopeType)) {
                    +irReturn(irCreateModuleScope())
                }
            }
        )
    }
    return field
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.irCreateModuleScope() = irCall(referenced(MokkeryIr.Function.createModuleScope)) {
    arguments[0] = irGetMokkeryScopeGlobal()
    arguments[1] = configuration.moduleName?.let(::irString) ?: irNull()
    arguments[2] = irGetEnumEntry(referenced(MokkeryIr.Class.MockMode), configuration.defaultMockMode)
    arguments[3] = irGetVerifyMode(configuration.defaultVerifyMode)
}


context(scope: TransformerScope)
private fun IrBuilderWithScope.irGetMokkeryScopeGlobal(): IrCall {
    val scopeCompanion = referencedCompanion(MokkeryIr.Class.MokkeryScope)
    return scopeCompanion
        .requirePropertyGetterOwner("global")
        .let { irCall(it) { arguments[0] = irGetObject(scopeCompanion.symbol) } }
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
