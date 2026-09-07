package dev.mokkery.plugin.ir.transformer.scope

import dev.mokkery.plugin.core.ir.pluginContext
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.declarationIrBuilder
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.overridePropertyBackingField
import dev.mokkery.plugin.ir.requirePropertyOwner
import dev.mokkery.plugin.ir.transformer.core.irGetMokkeryModuleScope
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.expressions.IrDelegatingConstructorCall
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.statements

private val supportedKinds = setOf(ClassKind.CLASS, ClassKind.OBJECT)

context(scope: TransformerScope)
fun IrClass.overrideMokkerySuiteScopeIfNotOverridden() {
    if (kind !in supportedKinds) return
    val mokkerySuiteScopeClass = referenced(MokkeryIr.Class.MokkerySuiteScope)
    if (superTypes.none { it.getClass() == mokkerySuiteScopeClass }) return
    val irClass = this
    val property = irClass.requirePropertyOwner("mokkeryContext")
    if (!property.isFakeOverride) return
    val initializedConstructors = irClass
        .constructors
        .filterNot { it.delegatesTo(irClass) }
        .toList()
    if (initializedConstructors.isEmpty()) return
    irClass.declarations.remove(property)
    val baseProperty = mokkerySuiteScopeClass.requirePropertyOwner("mokkeryContext")
    val newProperty = irClass.overridePropertyBackingField(context = pluginContext, property = baseProperty)
    initializedConstructors.forEach { constructor ->
        val oldStatements = constructor.body?.statements.orEmpty().toList()
        constructor.body = constructor.symbol.declarationIrBuilder {
            irBlockBody {
                oldStatements.forEach { statement ->
                    +statement
                    if (statement is IrDelegatingConstructorCall) {
                        +irSetField(
                            receiver = irGet(irClass.thisReceiver!!),
                            field = newProperty.backingField!!,
                            value = irCallSuiteContext(irClass)
                        )
                    }
                }
            }
        }
    }
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.irCallSuiteContext(
    irClass: IrClass
) = irCall(referenced(MokkeryIr.Function.suiteContext)) {
    val suiteNameClass = referenced(MokkeryIr.Class.SuiteName)
    arguments[0] = irGetMokkeryModuleScope()
    arguments[1] = irCallConstructor(suiteNameClass.primaryConstructor!!) {
        arguments[0] = irString(irClass.kotlinFqName.asString())
    }
}

private fun IrConstructor.delegatesTo(irClass: IrClass): Boolean = body
    ?.statements
    .orEmpty()
    .any { it.isDelegatingConstructorCallTo(irClass) }

private fun IrStatement.isDelegatingConstructorCallTo(
    irClass: IrClass
): Boolean = this is IrDelegatingConstructorCall && symbol.owner.parentAsClass == irClass
