package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.ext.getClass
import dev.mokkery.plugin.ext.irDefaultValue
import dev.mokkery.plugin.mokkeryError
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irReturnUnit
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationBase
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrSpreadElement
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.putElement
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

class CallTrackingNestedTransformer(
    private val pluginContext: IrPluginContext,
    private val irFile: IrFile,
    private val table: Map<IrClass, IrClass>,
    private val scopeVar: IrVariable,
    private val stubMockReturns: Boolean = false,
) : IrElementTransformerVoid() {

    private val localDeclarations = mutableListOf<IrDeclaration>()
    private val transformedTypes = mutableMapOf<IrExpression, IrType>()
    private val argMatchersScopeClass = pluginContext.getClass(Mokkery.ClassId.ArgMatchersScope)
    private val templatingContextClass = pluginContext.getClass(Mokkery.ClassId.TemplatingScope)

    override fun visitDeclaration(declaration: IrDeclarationBase): IrStatement {
        localDeclarations.add(declaration)
        return super.visitDeclaration(declaration)
    }

    override fun visitReturn(expression: IrReturn): IrExpression {
        if (!stubMockReturns) return super.visitReturn(expression)
        val value = expression.value as? IrCall ?: return super.visitReturn(expression)
        val dispatchReceiverClass = value.dispatchReceiver?.type?.getClass() ?: return super.visitReturn(expression)
        if (!table.containsKey(dispatchReceiverClass)) return super.visitReturn(expression)
        super.visitReturn(expression)
        return DeclarationIrBuilder(pluginContext, expression.returnTargetSymbol).run {
            irBlock {
                +expression.value
                val type = transformedTypes[expression.value] ?: expression.value.type
                if (type == pluginContext.irBuiltIns.unitType) {
                    +irReturnUnit()
                } else {
                    +irReturn(irDefaultValue(type))
                }
            }
        }
    }

    override fun visitCall(expression: IrCall): IrExpression {
        val dispatchReceiver = expression.dispatchReceiver ?: return super.visitCall(expression)
        if (!table.containsKey(dispatchReceiver.type.getClass())) {
            return super.visitCall(expression)
        }
        // make return type nullable to avoid runtime checks on non-primitive types (e.g. suspend fun on K/N)
        if (!expression.symbol.owner.returnType.isPrimitiveType(nullable = false)) {
            transformedTypes[expression] = expression.type
            expression.type = expression.type.makeNullable()
        }
        expression.dispatchReceiver = DeclarationIrBuilder(pluginContext, expression.symbol).run {
            irCall(templatingContextClass.getSimpleFunction("ensureBinding")!!).apply {
                this.dispatchReceiver = irGet(scopeVar)
                putTypeArgument(0, dispatchReceiver.type)
                putValueArgument(0, dispatchReceiver)
            }
        }
        interceptAllArgsOf(expression)
        return super.visitCall(expression)
    }

    private fun interceptAllArgsOf(expression: IrCall) {
        val extensionReceiver = expression.extensionReceiver
        val extensionReceiverParam = expression.symbol.owner.extensionReceiverParameter
        if (extensionReceiver != null && extensionReceiverParam != null) {
            expression.extensionReceiver = interceptArg(expression.symbol, extensionReceiverParam, extensionReceiver)
        }
        for (index in expression.valueArguments.indices) {
            val arg = expression.valueArguments[index] ?: continue
            val param = expression.symbol.owner.valueParameters[index]
            if (arg is IrGetValue && arg.interceptArgInitializer(param)) continue
            expression.putValueArgument(index, interceptArg(expression.symbol, param, arg))
        }
    }

    private fun checkOrigin(variable: IrVariable) {
        if (variable.origin == IrDeclarationOrigin.IR_TEMPORARY_VARIABLE) return
        val initializer = variable.initializer
        if (initializer !is IrCall) return
        val types = listOfNotNull(initializer.dispatchReceiver?.type, initializer.extensionReceiver?.type)
        if (types.any { it == argMatchersScopeClass.defaultType }) initializer.mokkeryError(irFile) {
            "Assigning matchers to variables is prohibited!"
        }
    }

    private fun interceptArg(symbol: IrSymbol, param: IrValueParameter, arg: IrExpression): IrExpression {
        return DeclarationIrBuilder(pluginContext, symbol).run {
            irCall(templatingContextClass.getSimpleFunction("interceptArg")!!).apply {
                this.dispatchReceiver = irGet(scopeVar)
                putValueArgument(0, irString(param.name.asString()))
                putValueArgument(1, interceptArgVarargs(arg))
            }
        }
    }

    private fun IrGetValue.interceptArgInitializer(param: IrValueParameter): Boolean {
        val owner = symbol.owner
        if (!localDeclarations.contains(owner)) return false
        if (owner !is IrVariable) return false
        return when (val initializer = owner.initializer) {
            is IrGetValue -> initializer.interceptArgInitializer(param)
            null -> false
            else -> {
                checkOrigin(owner)
                owner.initializer = interceptArg(owner.symbol, param, initializer)
                true
            }
        }
    }

    private fun DeclarationIrBuilder.interceptArgVarargs(arg: IrExpression): IrExpression {
        if (arg !is IrVararg) return arg
        arg.elements.forEachIndexed { index, element ->
            when (element) {
                is IrSpreadElement -> element.expression = interceptVarargElement(element.expression, true)
                is IrExpression -> arg.putElement(index, interceptVarargElement(element, false))
            }
        }
        return arg
    }

    private fun DeclarationIrBuilder.interceptVarargElement(expression: IrExpression, isSpread: Boolean): IrExpression {
        return irCall(templatingContextClass.getSimpleFunction("interceptVarargElement")!!).apply {
            this.dispatchReceiver = irGet(scopeVar)
            putValueArgument(0, expression)
            putValueArgument(1, irBoolean(isSpread))
        }
    }
}



