package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.ext.getClass
import dev.mokkery.plugin.mokkeryError
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationBase
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrDeclarationReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrSpreadElement
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.putElement
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

class CallTrackingNestedTransformer(
    private val pluginContext: IrPluginContext,
    private val irFile: IrFile,
    private val table: Map<IrClass, IrClass>
) : IrElementTransformerVoid() {

    private var firstDeclarationsToSkip = 2
    private lateinit var argMatchersScopeParam: IrValueParameter
    private val localDeclarations = mutableListOf<IrDeclaration>()
    private val _trackedExpressions = mutableMapOf<IrSymbolOwner, IrExpression>()
    private val argMatchersScopeClass = pluginContext.getClass(Mokkery.ClassId.ArgMatchersScope)
    val trackedExpressions: List<IrExpression> get() = _trackedExpressions.values.toList()

    override fun visitCall(expression: IrCall): IrExpression {
        val dispatchReceiver = expression.dispatchReceiver ?: return super.visitCall(expression)
        if (!table.containsKey(dispatchReceiver.type.getClass())) {
            return super.visitCall(expression)
        }
        // make return type nullable to avoid runtime checks on non-primitive types (e.g. suspend fun on K/N)
        if (!expression.type.isPrimitiveType(nullable = false)) {
            expression.type = expression.type.makeNullable()
        }
        interceptArgumentNames(expression)
        return super.visitCall(expression)
    }

    override fun visitDeclarationReference(expression: IrDeclarationReference): IrExpression {
        if (!table.containsKey(expression.type.getClass())) return super.visitDeclarationReference(expression)
        if (localDeclarations.contains(expression.symbol.owner)) return super.visitDeclarationReference(expression)
        _trackedExpressions[expression.symbol.owner] = expression
        return super.visitDeclarationReference(expression)
    }

    override fun visitDeclaration(declaration: IrDeclarationBase): IrStatement {
        if (firstDeclarationsToSkip > 0) {
            if (declaration is IrValueParameter) argMatchersScopeParam = declaration
            firstDeclarationsToSkip--
            return super.visitDeclaration(declaration)
        }
        localDeclarations.add(declaration)
        return super.visitDeclaration(declaration)
    }

    private fun interceptArgumentNames(expression: IrCall) {
        for (index in expression.valueArguments.indices) {
            val arg = expression.valueArguments[index] ?: continue
            val param = expression.symbol.owner.valueParameters[index]
            if (arg is IrGetValue && arg.interceptInitializerWithNamed(param)) continue
            expression.putValueArgument(index, interceptWithNamed(expression.symbol, param, arg))
        }
    }

    private fun interceptWithNamed(symbol: IrSymbol, param: IrValueParameter, arg: IrExpression): IrExpression {
        return DeclarationIrBuilder(pluginContext, symbol).run {
            irCall(argMatchersScopeClass.getSimpleFunction("named")!!).apply {
                this.dispatchReceiver = irGet(argMatchersScopeParam)
                putValueArgument(0, irString(param.name.asString()))
                putValueArgument(1, interceptArg(arg))
            }
        }
    }

    private fun DeclarationIrBuilder.interceptArg(arg: IrExpression): IrExpression {
        if (arg !is IrVararg) return arg
        arg.elements.forEachIndexed { index, element ->
            when (element) {
                is IrSpreadElement -> element.expression = interceptVarargElement(element.expression)
                is IrExpression -> arg.putElement(index, interceptVarargElement(element))
            }
        }
        return arg
    }

    private fun DeclarationIrBuilder.interceptVarargElement(expression: IrExpression): IrExpression {
        return irCall(argMatchersScopeClass.getSimpleFunction("varargElement")!!).apply {
            this.dispatchReceiver = irGet(argMatchersScopeParam)
            putValueArgument(0, expression)
        }
    }

    private fun IrGetValue.interceptInitializerWithNamed(param: IrValueParameter): Boolean {
        val owner = symbol.owner
        if (!localDeclarations.contains(owner)) return false
        if (owner !is IrVariable) return false
        return when (val initializer = owner.initializer) {
            is IrGetValue -> initializer.interceptInitializerWithNamed(param)
            null -> false
            else -> {
                checkOrigin(owner)
                owner.initializer = interceptWithNamed(owner.symbol, param, initializer)
                true
            }
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
}



