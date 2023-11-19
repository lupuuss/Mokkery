package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.Cache
import dev.mokkery.plugin.core.CompilerPluginScope
import dev.mokkery.plugin.core.CoreTransformer
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.Mokkery.Errors
import dev.mokkery.plugin.core.declarationIrBuilder
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.core.mockMode
import dev.mokkery.plugin.core.mokkeryErrorAt
import dev.mokkery.plugin.core.mokkeryLog
import dev.mokkery.plugin.core.mokkeryLogAt
import dev.mokkery.plugin.core.verifyMode
import dev.mokkery.plugin.ir.asTypeParamOrNull
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irGetEnumEntry
import dev.mokkery.plugin.ir.isAnyFunction
import dev.mokkery.plugin.ir.renderSymbol
import dev.mokkery.verify.SoftVerifyMode
import dev.mokkery.verify.VerifyMode
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.backend.js.utils.typeArguments
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.overrides.isOverridableProperty
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.defaultConstructor
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.isOverridable
import org.jetbrains.kotlin.ir.util.isTypeParameter
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.isSubpackageOf
import org.jetbrains.kotlin.platform.isJs
import kotlin.reflect.KClass
import kotlin.time.TimeSource

class MokkeryTransformer(compilerPluginScope: CompilerPluginScope) : CoreTransformer(compilerPluginScope) {

    private val mockCache = Cache<IrClass, IrClass>()
    private val spyCache = Cache<IrClass, IrClass>()

    private val internalEvery = getFunction(Mokkery.Function.internalEvery)
    private val internalEverySuspend = getFunction(Mokkery.Function.internalEverySuspend)
    private val internalVerify = getFunction(Mokkery.Function.internalVerify)
    private val internalVerifySuspend = getFunction(Mokkery.Function.internalVerifySuspend)

    override fun visitCall(expression: IrCall): IrExpression {
        val name = expression.symbol.owner.kotlinFqName
        if (!name.isSubpackageOf(Mokkery.dev_mokkery)) return super.visitCall(expression)
        val result = super.visitCall(expression)
        return when (name) {
            Mokkery.Name.mock -> replaceWithMock(expression)
            Mokkery.Name.spy -> replaceWithSpy(expression)
            Mokkery.Name.every -> replaceWithInternalEvery(expression, internalEvery.symbol)
            Mokkery.Name.verify -> replaceWithInternalVerify(expression, internalVerify.symbol)
            Mokkery.Name.everySuspend -> replaceWithInternalEvery(expression, internalEverySuspend.symbol)
            Mokkery.Name.verifySuspend -> replaceWithInternalVerify(expression, internalVerifySuspend.symbol)
            else -> result
        }
    }

    override fun visitFile(declaration: IrFile): IrFile {
        mockCache.clear()
        spyCache.clear()
        return super.visitFile(declaration)
    }

    override fun visitModuleFragment(declaration: IrModuleFragment): IrModuleFragment {
        val time = TimeSource.Monotonic.markNow()
        val result = super.visitModuleFragment(declaration)
        mokkeryLog { "Plugin time: ${time.elapsedNow()}" }
        return result
    }

    private fun replaceWithMock(call: IrCall): IrExpression {
        val klass = call.getTypeToMock() ?: return call
        if (pluginContext.platform.isJs() && klass.defaultType.isAnyFunction()) {
            return createMockJsFunction(call, klass)
        }
        val mockedClass = mockCache.getOrPut(klass) { createMockClass(klass) }
        return declarationIrBuilder(call) {
            irCallConstructor(mockedClass.primaryConstructor!!) {
                val modeArg = call.valueArguments
                    .getOrNull(0)
                    ?: irGetEnumEntry(getClass(Mokkery.Class.MockMode), mockMode.toString())
                val block = call.valueArguments.getOrNull(1)
                putValueArgument(0, modeArg)
                putValueArgument(1, block ?: irNull())
            }
        }
    }

    private fun replaceWithSpy(call: IrCall): IrExpression {
        val klass = call.getTypeToMock() ?: return call
        if (pluginContext.platform.isJs() && klass.defaultType.isAnyFunction()) {
            return createSpyJsFunction(call, klass)
        }
        val spiedClass = spyCache.getOrPut(klass) { createSpyClass(klass) }
        return declarationIrBuilder(call) {
            irCallConstructor(spiedClass.primaryConstructor!!) {
                putValueArgument(0, call.valueArguments[0])
            }
        }
    }

    private fun replaceWithInternalEvery(expression: IrCall, function: IrSimpleFunctionSymbol): IrExpression {
        val block = expression.getValueArgument(0)!!
        if (!block.assertFunctionExpressionThatOriginatesLambda(expression.symbol)) return expression
        return declarationIrBuilder(expression) {
            irBlock {
                val variable = createTmpVariable(irCall(getFunction(Mokkery.Function.TemplatingScope)))
                val transformer = TemplatingScopeCallsTransformer(
                    compilerPluginScope = this@MokkeryTransformer,
                    templatingScope = variable
                )
                block.transformChildren(transformer, null)
                +irCall(function) {
                    putValueArgument(0, irGet(variable))
                    putValueArgument(1, block)
                }
            }
        }
    }

    private fun replaceWithInternalVerify(expression: IrCall, function: IrSimpleFunctionSymbol): IrExpression {
        val mode = expression.getValueArgument(0)
        val block = expression.getValueArgument(1)!!
        if (!block.assertFunctionExpressionThatOriginatesLambda(expression.symbol)) return expression
        return declarationIrBuilder(expression) {
            irBlock {
                val variable = createTmpVariable(irCall(getFunction(Mokkery.Function.TemplatingScope)))
                val transformer = TemplatingScopeCallsTransformer(
                    compilerPluginScope = this@MokkeryTransformer,
                    templatingScope = variable
                )
                block.transformChildren(transformer, null)
                +irCall(function) {
                    putValueArgument(0, irGet(variable))
                    putValueArgument(1, mode ?: irGetVerifyMode(verifyMode))
                    putValueArgument(2, block)
                }
            }
        }
    }

    private fun IrBuilderWithScope.irGetVerifyMode(verifyMode: VerifyMode): IrExpression {
        val expression = when (verifyMode) {
            is SoftVerifyMode -> {
                irCallConstructor(getIrClassOf(SoftVerifyMode::class).primaryConstructor!!) {
                    putValueArgument(0, irInt(verifyMode.atLeast))
                    putValueArgument(1, irInt(verifyMode.atMost))
                }
            }
            else -> irGetObject(getIrClassOf(verifyMode::class).symbol)
        }
        return expression
    }

    // probably works only for top level classes
    private fun getIrClassOf(cls: KClass<*>) = pluginContext
        .referenceClass(ClassId.fromString(cls.qualifiedName!!.replace(".", "/")))!!
        .owner

    private fun IrExpression.assertFunctionExpressionThatOriginatesLambda(function: IrSimpleFunctionSymbol): Boolean {
        if (this !is IrFunctionExpression) {
            mokkeryErrorAt(this) {
                Errors.notLambdaExpression(
                    functionName = function.owner.name.asString(),
                    param = function.owner.valueParameters.last().renderSymbol(),
                )
            }
            return false
        }
        return true
    }

    private fun IrCall.getTypeToMock(): IrClass? {
        if (!checkInterceptionPossibilities()) return null
        return this.type.getClass()
    }

    private fun IrCall.checkInterceptionPossibilities(): Boolean {
        val name = symbol.owner.name.asString()
        val typeArg = typeArguments.firstOrNull()
        val typeToMock = typeArg
            ?.takeIf { !it.isTypeParameter() }
            ?: run {
                mokkeryErrorAt(this) { Errors.indirectCall(typeArg?.render().orEmpty(), name) }
                return false
            }
        val classToMock = typeToMock.getClass()!!
        if (classToMock.modality == Modality.SEALED) {
            mokkeryErrorAt(this) {
                Errors.sealedTypeCannotBeIntercepted(typeName = classToMock.kotlinFqName.asString(), functionName = name)
            }
            return false
        }
        if (classToMock.modality == Modality.FINAL) {
            mokkeryErrorAt(this) {
                Errors.finalTypeCannotBeIntercepted(typeName = classToMock.kotlinFqName.asString(), functionName = name)
            }
            return false
        }
        if (!classToMock.isInterface && classToMock.defaultConstructor == null) {
            mokkeryErrorAt(this) {
                Errors.noDefaultConstructorTypeCannotBeIntercepted(
                    typeName = classToMock.kotlinFqName.asString(),
                    functionName = name
                )
            }
            return false
        }
        val nonOverridableFunctions = classToMock.functions.filterNot { it.isOverridable }
        val nonOverridableProperties = classToMock.properties.filterNot { it.isOverridableProperty() }
        if (nonOverridableProperties.any() || nonOverridableFunctions.any()) {
            mokkeryErrorAt(this) {
                val names = nonOverridableFunctions
                    .map(IrSimpleFunction::renderSymbol)
                    .plus(nonOverridableProperties.map(IrProperty::renderSymbol))
                Errors.finalMembersTypeCannotBeIntercepted(
                    typeName = classToMock.kotlinFqName.asString(),
                    functionName = name,
                    nonAbstractMembers = names.joinToString()
                )
            }
            return false
        }
        mokkeryLogAt(this) { "Recognized $name call with type ${typeToMock.render()}!" }
        return true
    }
}
