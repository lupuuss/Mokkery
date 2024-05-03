package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.Cache
import dev.mokkery.plugin.core.CompilerPluginScope
import dev.mokkery.plugin.core.CoreTransformer
import dev.mokkery.plugin.core.MembersValidationMode
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.Mokkery.Errors
import dev.mokkery.plugin.core.declarationIrBuilder
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.core.mockMode
import dev.mokkery.plugin.core.mokkeryErrorAt
import dev.mokkery.plugin.core.mokkeryLog
import dev.mokkery.plugin.core.mokkeryLogAt
import dev.mokkery.plugin.core.validationMode
import dev.mokkery.plugin.core.verifyMode
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irGetEnumEntry
import dev.mokkery.plugin.ir.isAnyFunction
import dev.mokkery.plugin.ir.isOverridable
import dev.mokkery.plugin.ir.isPlatformDependent
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
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.defaultConstructor
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.isClass
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
    private val mockManyCache = Cache<Set<IrClass>, IrClass>()
    private val spyCache = Cache<IrClass, IrClass>()

    private val internalEvery = getFunction(Mokkery.Function.internalEvery)
    private val internalEverySuspend = getFunction(Mokkery.Function.internalEverySuspend)
    private val internalVerify = getFunction(Mokkery.Function.internalVerify)
    private val internalVerifySuspend = getFunction(Mokkery.Function.internalVerifySuspend)
    private val validationMode: MembersValidationMode = compilerConfig.validationMode

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
        val klass = call.getTypeToMock() ?: return call
        if (pluginContext.platform.isJs() && klass.defaultType.isAnyFunction()) {
            return createMockJsFunction(call, klass)
        }
        val mockedClass = mockCache.getOrPut(klass) { createMockClass(klass).also(currentFile::addChild) }
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

    private fun replaceWithMockMany(call: IrCall): IrExpression {
        val classes = call.getTypesToMock()
        if (classes.isEmpty()) return call
        val mockedClass = mockManyCache.getOrPut(classes) {
            createManyMockClass(classes.toList()).also(currentFile::addChild)
        }
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
        val spiedClass = spyCache.getOrPut(klass) { createSpyClass(klass).also(currentFile::addChild) }
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
                val transformer = TemplatingScopeCallsTransformer(this@MokkeryTransformer, variable)
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
        if (!checkInterceptionPossibilities(typeArguments.firstOrNull())) return null
        return this.type.getClass()
    }

    private fun IrCall.getTypesToMock(): Set<IrClass> {
        val name = symbol.owner.name.asString()
        if (typeArguments.any { !checkInterceptionPossibilities(it) }) return emptySet()
        val noDistinctClasses = typeArguments.mapNotNull { it?.getClass() }
        val classes = noDistinctClasses.toSet()
        if (classes.size != noDistinctClasses.size) {
            val duplicates = noDistinctClasses.groupBy { it }.entries.first { it.value.size > 1 }
            mokkeryErrorAt(this) {
                Errors.noDuplicatesForMockMany(
                    typeName = duplicates.key.kotlinFqName.asString(),
                    occurrences = duplicates.value.size,
                    functionName = name
                )
            }
            return emptySet()
        }
        if (classes.count { it.isClass } > 1) {
            mokkeryErrorAt(this) {
                val superClasses = classes.filter(IrClass::isClass).joinToString { it.kotlinFqName.asString() }
                Errors.singleSuperClass(name, superClasses)
            }
            return emptySet()
        }
        if (pluginContext.platform.isJs()) {
            val functionalType = classes.find { it.isFun }
            if (functionalType != null) {
                mokkeryErrorAt(this) {
                    Errors.functionalTypeNotAllowedOnJs(functionalType.kotlinFqName.asString(), name)
                }
                return emptySet()
            }
        }
        return classes
    }

    private fun IrCall.checkInterceptionPossibilities(typeArg: IrType?): Boolean {
        val name: String = symbol.owner.name.asString()
        val typeToMock = typeArg
            ?.takeIf { !it.isTypeParameter() }
            ?: run {
                mokkeryErrorAt(this) { Errors.indirectCall(typeArg?.render().orEmpty(), name) }
                return false
            }
        if (typeToMock.isAnyFunction()) return true
        if (typeToMock.isPrimitiveType()) {
            mokkeryErrorAt(this) {
                Errors.primitiveTypeCannotBeIntercepted(typeName = typeToMock.render(), functionName = name)
            }
            return false
        }
        val classToMock = typeToMock.getClass()!!
        if (classToMock.modality == Modality.SEALED) {
            mokkeryErrorAt(this) {
                Errors.sealedTypeCannotBeIntercepted(
                    typeName = classToMock.kotlinFqName.asString(),
                    functionName = name
                )
            }
            return false
        }
        if (classToMock.isInterface) return true
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
        val illegalFunctions = classToMock
            .functions
            .filterNot { it.isValid(validationMode) }
        val illegalProperties = classToMock
            .properties
            .filterNot { it.isValid(validationMode) }
        if (illegalProperties.any() || illegalFunctions.any()) {
            mokkeryErrorAt(this) {
                val names = illegalFunctions
                    .map(IrSimpleFunction::renderSymbol)
                    .plus(illegalProperties.map(IrProperty::renderSymbol))
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

    private fun IrSimpleFunction.isValid(validationMode: MembersValidationMode): Boolean {
        if (isOverridable) return true
        return when (validationMode) {
            MembersValidationMode.Strict -> false
            MembersValidationMode.IgnoreInline -> isInline
            MembersValidationMode.IgnoreFinal -> true
        }
    }

    private fun IrProperty.isValid(validationMode: MembersValidationMode): Boolean {
        if (isOverridable) return true
        return when (validationMode) {
            MembersValidationMode.Strict -> false
            MembersValidationMode.IgnoreInline -> isInline
            MembersValidationMode.IgnoreFinal -> true
        }
    }

    private val IrProperty.isInline: Boolean
        get() {
            val getter = getter
            val setter = setter
            return (getter == null || getter.isInline) && (setter == null || setter.isInline)
        }
}
