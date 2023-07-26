package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.ext.buildClass
import dev.mokkery.plugin.ext.createUniqueMockName
import dev.mokkery.plugin.ext.defaultTypeErased
import dev.mokkery.plugin.ext.eraseFullValueParametersList
import dev.mokkery.plugin.ext.irCallConstructor
import dev.mokkery.plugin.ext.irInvoke
import dev.mokkery.plugin.ext.irLambda
import dev.mokkery.plugin.ext.irTryCatchAny
import dev.mokkery.plugin.ext.isAnyFunction
import dev.mokkery.plugin.ext.overrideAllOverridableFunctions
import dev.mokkery.plugin.ext.overrideAllOverridableProperties
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.backend.js.utils.typeArguments
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.declarations.addField
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irIfThenElse
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.platform.isJs

class SpyCallsTransformer(
    pluginContext: IrPluginContext,
    messageCollector: MessageCollector,
    irFile: IrFile,
    private val spyTable: MutableMap<IrClass, IrClass>
) : MokkeryBaseTransformer(pluginContext, messageCollector, irFile) {

    override fun visitCall(expression: IrCall): IrExpression {
        val function = expression.symbol.owner
        if (function.kotlinFqName != Mokkery.Function.spy) return super.visitCall(expression)
        expression.checkInterceptionPossibilities(Mokkery.Function.spy)
        val typeToSpy = expression.typeArguments.first()!!
        val classToSpy = typeToSpy.getClass()!!
        if (pluginContext.platform.isJs() && typeToSpy.isAnyFunction()) {
            return handleJsFunctionSpying(expression, classToSpy)
        }
        return handleRegularSpy(expression, classToSpy)
    }

    private fun handleJsFunctionSpying(expression: IrCall, classToSpy: IrClass): IrExpression {
        val anyNType = pluginContext.irBuiltIns.anyNType
        val typeToSpy = classToSpy.defaultTypeErased
        val returnType = typeToSpy.let { it as IrSimpleType }.arguments.last().typeOrNull ?: anyNType
        return DeclarationIrBuilder(pluginContext, expression.symbol).run {
            irBlock {
                val spiedObj = expression.valueArguments[0]!!
                val mokkeryScopeCall = irCall(irFunctions.MokkerySpyScope).apply {
                    putValueArgument(0, irString(typeToSpy.classFqName!!.asString()))
                }
                val scopeVar = createTmpVariable(mokkeryScopeCall)
                val lambda = irLambda(returnType, typeToSpy, irFile) { lambdaFun ->
                    val expr = irIfThenElse(
                        type = returnType,
                        condition = irCallIsTemplatingEnabled(irGet(scopeVar)),
                        thenPart = irCallInterceptOn(irGet(scopeVar), lambdaFun),
                        elsePart = irBlock {
                            val args = lambdaFun.valueParameters.map { irGet(it) }.toTypedArray()
                            +irTryCatchAny(irCallInterceptOn(irGet(scopeVar), lambdaFun))
                            +irReturn(irInvoke(spiedObj, lambdaFun.isSuspend, *args))
                        }
                    )
                    +irReturn(expr)
                }
                val lambdaVar = createTmpVariable(lambda)
                +irCallRegisterScope(irGet(lambdaVar), irGet(scopeVar))
                +irGet(lambdaVar)
            }
        }
    }

    private fun handleRegularSpy(expression: IrCall, classToSpy: IrClass): IrExpression {
        val spiedClass = spyTable.getOrPut(classToSpy) {
            declareSpy(classToSpy).also {
                irFile.addChild(it)
            }
        }
        return DeclarationIrBuilder(pluginContext, expression.symbol).run {
            irCallConstructor(spiedClass.primaryConstructor!!).also {
                it.putValueArgument(0, expression.valueArguments[0])
            }
        }
    }

    private fun declareSpy(classToSpy: IrClass): IrClass {
        val typeToMockProjected = classToSpy.defaultTypeErased
        val newClass = pluginContext.irFactory.buildClass(
            classToSpy.createUniqueMockName("Spy"),
            typeToMockProjected,
            irClasses.MokkerySpyScope.defaultType,
            if (classToSpy.isInterface) pluginContext.irBuiltIns.anyType else null
        )
        val delegateField = newClass.addField(fieldName = "delegate", typeToMockProjected)
        newClass.inheritMokkeryInterceptor(
            interceptorScopeClass = irClasses.MokkerySpyScope,
            classToMock = classToSpy,
            interceptorInit = { constructor ->
                constructor.addValueParameter("obj", typeToMockProjected)
                +irSetField(irGet(newClass.thisReceiver!!), delegateField, irGet(constructor.valueParameters[0]))
                irCall(irFunctions.MokkerySpy)
            }
        )
        newClass.overrideAllOverridableFunctions(pluginContext, classToSpy) { spyingBody(delegateField, it) }
        newClass.overrideAllOverridableProperties(
            context = pluginContext,
            superClass = classToSpy,
            getterBlock = { spyingBody(delegateField, it) },
            setterBlock = { spyingBody(delegateField, it) }
        )
        return newClass
    }

    private fun IrBlockBodyBuilder.spyingBody(delegateField: IrField, function: IrSimpleFunction) {
        function.eraseFullValueParametersList()
        +irIfThenElse(
            type = function.returnType,
            condition = irCallIsTemplatingEnabled(irGet(function.dispatchReceiverParameter!!)),
            thenPart = irReturn(irCallInterceptingMethod(function)),
            elsePart = irBlock {
                +irTryCatchAny(irCallInterceptingMethod(function))
                +irReturn(irCall(function.overriddenSymbols.first()).apply {
                    dispatchReceiver = irGetField(irGet(function.dispatchReceiverParameter!!), delegateField)
                    function.valueParameters.forEachIndexed { index, irValueParameter ->
                        putValueArgument(index, irGet(irValueParameter))
                    }
                    function.extensionReceiverParameter?.let {
                        extensionReceiver = irGet(it)
                    }
                })
            }
        )
    }

    private fun IrBlockBodyBuilder.irCallIsTemplatingEnabled(scope: IrExpression): IrCall {
        val templatingAccess = irCall(irClasses.MokkerySpy.symbol.getPropertyGetter("templating")!!)
        templatingAccess.dispatchReceiver = irCall(irClasses.MokkerySpyScope.getPropertyGetter("interceptor")!!).apply {
            dispatchReceiver = scope
        }
        val isEnabled = irCall(irClasses.TemplatingInterceptor.symbol.getPropertyGetter("isEnabled")!!).apply {
            dispatchReceiver = templatingAccess
        }
        return isEnabled
    }
}
