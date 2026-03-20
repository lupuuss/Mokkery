package dev.mokkery.plugin.ir.transformer.suite

import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.overridePropertyBackingField
import dev.mokkery.plugin.ir.pluginContext
import dev.mokkery.plugin.ir.requirePropertyOwner
import dev.mokkery.plugin.ir.transformer.core.TransformerScope
import dev.mokkery.plugin.ir.transformer.core.declarationIrBuilder
import dev.mokkery.plugin.ir.transformer.core.referenced
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.statements

context(scope: TransformerScope)
fun IrClass.overrideMokkerySuiteScopeIfNotOverridden() {
    val irClass = this
    val mokkerySuiteScopeClass = referenced(MokkeryIr.Class.MokkerySuiteScope)
    val suiteNameClass = referenced(MokkeryIr.Class.SuiteName)
    val property = irClass.requirePropertyOwner("mokkeryContext")
    if (!property.isFakeOverride) return
    irClass.declarations.remove(property)
    val baseProperty = mokkerySuiteScopeClass.requirePropertyOwner("mokkeryContext")
    val newProperty = irClass.overridePropertyBackingField(context = pluginContext, property = baseProperty)
    val constructor = irClass.primaryConstructor!!
    val oldBody = constructor.body
    constructor.body = constructor.symbol.declarationIrBuilder {
        irBlockBody {
            val testScopeFun = referenced(MokkeryIr.Function.MokkerySuiteScope)
            val getContext = irCall(baseProperty.getter!!) {
                arguments[0] = irCall(testScopeFun) {
                    val testsScopeName = irCallConstructor(suiteNameClass.primaryConstructor!!) {
                        arguments[0] = irString(irClass.kotlinFqName.asString())
                    }
                    arguments[0] = testsScopeName
                }
            }
            +irSetField(irGet(irClass.thisReceiver!!), newProperty.backingField!!, getContext)
            oldBody?.statements?.forEach { it.unaryPlus() }
        }
    }
}
