package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.CompilerPluginScope
import dev.mokkery.plugin.core.CoreTransformer
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.declarationIrBuilder
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.core.messageCollector
import dev.mokkery.plugin.core.mokkeryErrorAt
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irLambda
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.logAt
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationBase
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrSpreadElement
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.putElement
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.isNothing
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.isFinalClass
import org.jetbrains.kotlin.ir.util.isSuspend
import org.jetbrains.kotlin.ir.util.isTypeParameter
import org.jetbrains.kotlin.platform.konan.isNative

class TemplatingScopeCallsTransformer(
    compilerPluginScope: CompilerPluginScope,
    private val templatingScope: IrVariable,
) : CoreTransformer(compilerPluginScope) {

    private val isNativePlatform = pluginContext.platform.isNative()
    private var token = 0
    private val localDeclarations = mutableListOf<IrDeclaration>()
    private val argMatchersScopeClass = getClass(Mokkery.Class.ArgMatchersScope)
    private val templatingContextClass = getClass(Mokkery.Class.TemplatingScope)

    override fun visitDeclaration(declaration: IrDeclarationBase): IrStatement {
        localDeclarations.add(declaration)
        return super.visitDeclaration(declaration)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        val receiver = expression.dispatchReceiver ?: return super.visitCall(expression)
        val cls = receiver.type.getClass() ?: return super.visitCall(expression)
        if (cls.isFinalClass) return super.visitCall(expression)
        super.visitCall(expression)
        val returnType = expression.symbol.owner.returnType
        expression.dispatchReceiver = declarationIrBuilder(expression) {
            irBlock {
                val tmp = createTmpVariable(receiver)
                +irCall(templatingContextClass.getSimpleFunction("ensureBinding")!!) {
                    val genericReturnTypeHint = when {
                        !returnType.isTypeParameter() || expression.type.isTypeParameter() -> irNull()
                        else ->  kClassReference(expression.type)
                    }
                    dispatchReceiver = irGet(templatingScope)
                    putValueArgument(0, irInt(token))
                    putValueArgument(1, irGet(tmp))
                    putValueArgument(2, genericReturnTypeHint)
                }
                +irGet(tmp)
            }
        }
        interceptAllArgsOf(expression)
        token++
        return when {
            isNativePlatform -> workaroundNativeChecks(expression, returnType)
            else -> expression
        }
    }

    private fun workaroundNativeChecks(expression: IrCall, returnType: IrType): IrCall {
        if (returnType.isPrimitiveType(nullable = false) || returnType.isNothing()) return expression
        expression.apply { type = pluginContext.irBuiltIns.anyNType }
        if (!expression.isSuspend || expression.valueArguments.all { it != null }) return expression
        val callIgnoringClassCastExceptionFun = getFunction(Mokkery.Function.callIgnoringClassCastException)
        return declarationIrBuilder(expression) {
            irCall(callIgnoringClassCastExceptionFun) {
                val blockParamType = callIgnoringClassCastExceptionFun.valueParameters[1].type
                val lambda = irLambda(pluginContext.irBuiltIns.anyNType, blockParamType, currentFile) { +expression }
                putValueArgument(0, irGet(templatingScope))
                putValueArgument(1, lambda)
                putTypeArgument(0, pluginContext.irBuiltIns.anyNType)
            }
        }
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
        if (types.any { it == argMatchersScopeClass.defaultType }) {
            mokkeryErrorAt(initializer) { "Assigning matchers to variables is prohibited!" }
        }
    }

    private fun interceptArg(
        symbol: IrSymbol,
        param: IrValueParameter,
        arg: IrExpression
    ): IrExpression = declarationIrBuilder(symbol) {
        irCall(templatingContextClass.getSimpleFunction("interceptArg")!!) {
            this.type = arg.type.makeNullable()
            this.dispatchReceiver = irGet(templatingScope)
            putTypeArgument(0, arg.type.makeNullable())
            putValueArgument(0, irInt(token))
            putValueArgument(1, irString(param.name.asString()))
            putValueArgument(2, interceptArgVarargs(arg))
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
        return irCall(templatingContextClass.getSimpleFunction("interceptVarargElement")!!) {
            this.type = expression.type
            dispatchReceiver = irGet(templatingScope)
            putValueArgument(0, irInt(token))
            putValueArgument(1, expression)
            putValueArgument(2, irBoolean(isSpread))
        }
    }
}



