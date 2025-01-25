package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.Cache
import dev.mokkery.plugin.core.CompilerPluginScope
import dev.mokkery.plugin.core.CoreTransformer
import dev.mokkery.plugin.core.IrMokkeryKind.Mock
import dev.mokkery.plugin.core.IrMokkeryKind.Spy
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.declarationIrBuilder
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.core.mockMode
import dev.mokkery.plugin.core.mokkeryLog
import dev.mokkery.plugin.core.platform
import dev.mokkery.plugin.core.verifyMode
import dev.mokkery.plugin.ir.forEachIndexedTypeArgument
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irGetEnumEntry
import dev.mokkery.plugin.ir.isAnyFunction
import dev.mokkery.plugin.ir.isPlatformDependent
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.verify.SoftVerifyMode
import dev.mokkery.verify.VerifyMode
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
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.isSubpackageOf
import org.jetbrains.kotlin.platform.isJs
import kotlin.reflect.KClass
import kotlin.time.TimeSource

class MokkeryTransformer(compilerPluginScope: CompilerPluginScope) : CoreTransformer(compilerPluginScope) {

    private val mockCache = Cache<IrClass, IrClass>()
    private val mockManyCache = Cache<Set<IrClass>, IrClass>()
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
            Mokkery.Name.mockMany -> replaceWithMockMany(expression)
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
        mockManyCache.clear()
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
        val typeToMock = call.typeArguments.firstOrNull() ?: return call
        val classToMock = typeToMock.getClass() ?: return call
        if (platform.isJs() && classToMock.defaultType.isAnyFunction()) return buildMockJsFunction(call, Mock)
        val mockedClass = mockCache.getOrPut(classToMock) { buildMockClass(Mock, classToMock).also(currentFile::addChild) }
        return declarationIrBuilder(call) {
            irCallConstructor(mockedClass.primaryConstructor!!) {
                val modeArg = call.valueArguments.getOrNull(0)
                    ?: irGetEnumEntry(getClass(Mokkery.Class.MockMode), mockMode.toString())
                val block = call.valueArguments.getOrNull(1)
                putValueArgument(0, irGetObject(getClass(Mokkery.Class.GlobalMokkeryScope).symbol))
                putValueArgument(1, modeArg)
                putValueArgument(2, block ?: irNull())
                val anyType = context.irBuiltIns.anyType
                typeToMock.forEachIndexedTypeArgument { index, it ->
                    putValueArgument(3 + index, kClassReference(it ?: anyType))
                }
            }
        }
    }

    private fun replaceWithMockMany(call: IrCall): IrExpression {
        val classes = call.typeArguments.mapNotNullTo(mutableSetOf()) { it?.getClass() }
        if (classes.isEmpty()) return call
        val mockedClass = mockManyCache.getOrPut(classes) {
            buildManyMockClass(classes.toList()).also(currentFile::addChild)
        }
        return declarationIrBuilder(call) {
            irCallConstructor(mockedClass.primaryConstructor!!) {
                val modeArg = call.valueArguments.getOrNull(0)
                    ?: irGetEnumEntry(getClass(Mokkery.Class.MockMode), mockMode.toString())
                putValueArgument(0, irGetObject(getClass(Mokkery.Class.GlobalMokkeryScope).symbol))
                putValueArgument(1, modeArg)
                putValueArgument(2, call.valueArguments.getOrNull(1) ?: irNull())
                val anyType = context.irBuiltIns.anyType
                call.typeArguments
                    .filterNotNull()
                    .forEachIndexedTypeArgument { index, it ->
                        putValueArgument(3 + index, kClassReference(it ?: anyType))
                    }
            }
        }
    }

    private fun replaceWithSpy(call: IrCall): IrExpression {
        val typeToMock = call.typeArguments.firstOrNull() ?: return call
        val klass = typeToMock.getClass() ?: return call
        if (platform.isJs() && klass.defaultType.isAnyFunction()) return buildMockJsFunction(call, Spy)
        val spiedClass = spyCache.getOrPut(klass) { buildMockClass(Spy, klass).also(currentFile::addChild) }
        return declarationIrBuilder(call) {
            irCallConstructor(spiedClass.primaryConstructor!!) {
                val block = call.valueArguments.getOrNull(1) ?: irNull()
                putValueArgument(0, irGetObject(getClass(Mokkery.Class.GlobalMokkeryScope).symbol))
                putValueArgument(1, irGetEnumEntry(getClass(Mokkery.Class.MockMode), "strict"))
                putValueArgument(2, block)
                putValueArgument(3, call.valueArguments[0])
                val anyType = context.irBuiltIns.anyType
                typeToMock.forEachIndexedTypeArgument { index, it ->
                    putValueArgument(4 + index, kClassReference(it ?: anyType))
                }
            }
        }
    }

    private fun replaceWithInternalEvery(expression: IrCall, function: IrSimpleFunctionSymbol): IrExpression {
        val block = expression.getValueArgument(0)!!
        return declarationIrBuilder(expression) {
            irBlock {
                val variable = createTmpVariable(irCall(getFunction(Mokkery.Function.TemplatingScope)))
                val transformer = TemplatingScopeCallsTransformer(this@MokkeryTransformer, variable)
                transformer.currentFile = currentFile
                block.transformChildren(transformer, null)
                +irCall(function) {
                    block as IrFunctionExpression
                    // make return type nullable to avoid runtime checks for primitive types (required by Wasm-JS)
                    if (block.function.returnType.isPlatformDependent()) {
                        putTypeArgument(0, getTypeArgument(0)?.makeNullable())
                        block.function.returnType = block.function.returnType.makeNullable()
                    }
                    putValueArgument(0, irGet(variable))
                    putValueArgument(1, block)
                }
            }
        }
    }

    private fun replaceWithInternalVerify(expression: IrCall, function: IrSimpleFunctionSymbol): IrExpression {
        val mode = expression.getValueArgument(0)
        val block = expression.getValueArgument(1)!!
        return declarationIrBuilder(expression) {
            irBlock {
                val variable = createTmpVariable(irCall(getFunction(Mokkery.Function.TemplatingScope)))
                val transformer = TemplatingScopeCallsTransformer(
                    compilerPluginScope = this@MokkeryTransformer,
                    templatingScope = variable
                )
                transformer.currentFile = currentFile
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
            is SoftVerifyMode -> irCallConstructor(getIrClassOf(SoftVerifyMode::class).primaryConstructor!!) {
                putValueArgument(0, irInt(verifyMode.atLeast))
                putValueArgument(1, irInt(verifyMode.atMost))
            }
            else -> irGetObject(getIrClassOf(verifyMode::class).symbol)
        }
        return expression
    }

    // probably works only for top level classes
    private fun getIrClassOf(cls: KClass<*>) = pluginContext
        .referenceClass(ClassId.fromString(cls.qualifiedName!!.replace(".", "/")))!!
        .owner
}
