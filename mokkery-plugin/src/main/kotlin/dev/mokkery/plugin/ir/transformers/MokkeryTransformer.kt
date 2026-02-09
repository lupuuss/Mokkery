package dev.mokkery.plugin.ir.transformers

import dev.mokkery.plugin.Cache
import dev.mokkery.plugin.ir.transformers.core.CompilerPluginScope
import dev.mokkery.plugin.ir.transformers.core.CoreTransformer
import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.ir.transformers.core.declarationIrBuilder
import dev.mokkery.plugin.ir.transformers.core.getClass
import dev.mokkery.plugin.ir.transformers.core.getCompanionOf
import dev.mokkery.plugin.ir.transformers.core.getFunction
import dev.mokkery.plugin.ir.transformers.core.mockMode
import dev.mokkery.plugin.ir.transformers.core.mokkeryLog
import dev.mokkery.plugin.ir.transformers.core.platform
import dev.mokkery.plugin.ir.transformers.core.verifyMode
import dev.mokkery.plugin.ir.IrMokkeryKind.Mock
import dev.mokkery.plugin.ir.IrMokkeryKind.Spy
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.asTypeParamOrNull
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.forEachIndexedTypeArgument
import dev.mokkery.plugin.ir.getProperty
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irCallMapOf
import dev.mokkery.plugin.ir.irGetEnumEntry
import dev.mokkery.plugin.ir.irLambdaOf
import dev.mokkery.plugin.ir.isAnyFunction
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.overridePropertyBackingField
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verify.VerifyModeInternals
import dev.mokkery.verify.VerifyModeInternals.Soft
import org.jetbrains.kotlin.backend.common.ir.moveBodyTo
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBlockBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionReference
import org.jetbrains.kotlin.ir.expressions.IrPropertyReference
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.makeNotNull
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.eraseTypeParameters
import org.jetbrains.kotlin.ir.util.isClass
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.nestedClasses
import org.jetbrains.kotlin.ir.util.nonDispatchParameters
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.isSubpackageOf
import org.jetbrains.kotlin.platform.isJs
import org.jetbrains.kotlin.utils.memoryOptimizedMap
import kotlin.reflect.KClass
import kotlin.time.TimeSource

class MokkeryTransformer(compilerPluginScope: CompilerPluginScope) : CoreTransformer(compilerPluginScope) {

    private val mockCache = Cache<IrClass, IrClass>()
    private val mockManyCache = Cache<Set<IrClass>, IrClass>()
    private val spyCache = Cache<IrClass, IrClass>()

    private val internalEvery = getFunction(MokkeryIr.Function.internalEvery)
    private val internalEverySuspend = getFunction(MokkeryIr.Function.internalEverySuspend)
    private val internalVerify = getFunction(MokkeryIr.Function.internalVerify)
    private val internalVerifySuspend = getFunction(MokkeryIr.Function.internalVerifySuspend)
    private val mokkeryScopeCompanion = getCompanionOf(MokkeryIr.Class.MokkeryScope)
    private val mokkerySuiteScopeClass = getClass(MokkeryIr.Class.MokkerySuiteScope)
    private val suiteNameClass = getClass(MokkeryIr.Class.SuiteName)
    private val matchersCompiler = MatchersCompiler(this)

    override fun visitClassNew(declaration: IrClass): IrStatement {
        overrideMokkerySuiteScopeIfNotOverridden(declaration)
        return super.visitClassNew(declaration)
    }

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        if (declaration is IrSimpleFunction) matchersCompiler.compileIfMatcher(declaration)
        return super.visitFunctionNew(declaration)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        val name = expression.symbol.owner.kotlinFqName
        expression.transformChildrenVoid()
        if (!name.isSubpackageOf(Mokkery.dev_mokkery)) return expression
        return when (name) {
            Mokkery.Name.mock -> replaceWithMock(expression)
            Mokkery.Name.mockMany -> replaceWithMockMany(expression)
            Mokkery.Name.spy -> replaceWithSpy(expression)
            Mokkery.Name.every -> replaceWithInternalEvery(expression, internalEvery.symbol)
            Mokkery.Name.verify -> replaceWithInternalVerify(expression, internalVerify.symbol)
            Mokkery.Name.everySuspend -> replaceWithInternalEvery(expression, internalEverySuspend.symbol)
            Mokkery.Name.verifySuspend -> replaceWithInternalVerify(expression, internalVerifySuspend.symbol)
            else -> expression
        }
    }

    override fun visitFileNew(declaration: IrFile): IrFile {
        mockCache.clear()
        mockManyCache.clear()
        spyCache.clear()
        return super.visitFileNew(declaration)
    }

    override fun visitModuleFragment(declaration: IrModuleFragment): IrModuleFragment {
        val time = TimeSource.Monotonic.markNow()
        val result = super.visitModuleFragment(declaration)
        mokkeryLog { "Plugin time: ${time.elapsedNow()}" }
        return result
    }

    private fun overrideMokkerySuiteScopeIfNotOverridden(irClass: IrClass) {
        if (!irClass.isClass) return
        if (irClass.superTypes.none { it.getClass() == mokkerySuiteScopeClass }) return
        val property = irClass.getProperty("mokkeryContext")
        if (!property.isFakeOverride) return
        irClass.declarations.remove(property)
        val baseProperty = mokkerySuiteScopeClass.getProperty("mokkeryContext")
        val newProperty = irClass.overridePropertyBackingField(context = pluginContext, property = baseProperty)
        val constructor = irClass.primaryConstructor!!
        val oldBody = constructor.body
        constructor.body = declarationIrBuilder {
            irBlockBody {
                val testScopeFun = getFunction(MokkeryIr.Function.MokkerySuiteScope)
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

    private fun replaceWithMock(call: IrCall): IrExpression {
        val typeToMock = call.typeArguments.firstOrNull() ?: return call
        val classToMock = typeToMock.getClass() ?: return call
        if (platform.isJs() && classToMock.defaultType.isAnyFunction()) return buildMockJsFunction(call, Mock)
        val mockedClass = mockCache.getOrPut(classToMock) {
            buildMockClass(Mock, classToMock)
        }
        return declarationIrBuilder {
            irCallConstructor(mockedClass.primaryConstructor!!) {
                val calledFun = call.symbol.owner
                val extensionParam = calledFun.parameters.find { it.kind == IrParameterKind.ExtensionReceiver }
                val regularParams = calledFun.parameters - extensionParam
                arguments[0] = extensionParam?.let(call.arguments::get) ?: getMokkeryGlobalScope()
                arguments[1] = call.arguments[regularParams[0]!!] ?: irGetEnumEntry(
                    getClass(MokkeryIr.Class.MockMode),
                    mockMode.toString()
                )
                arguments[2] = call.arguments[regularParams[1]!!] ?: irNull()
                val anyType = context.irBuiltIns.anyType
                typeToMock.forEachIndexedTypeArgument { index, it ->
                    arguments[3 + index] = kClassReference(it ?: anyType)
                }
            }
        }
    }

    private fun replaceWithMockMany(call: IrCall): IrExpression {
        val classes = call.typeArguments.mapNotNullTo(mutableSetOf()) { it?.getClass() }
        if (classes.isEmpty()) return call
        val mockedClass = mockManyCache.getOrPut(classes) {
            buildManyMockClass(classes.toList())
        }
        return declarationIrBuilder {
            irCallConstructor(mockedClass.primaryConstructor!!) {
                val calledFun = call.symbol.owner
                val extensionParam = calledFun.parameters.find { it.kind == IrParameterKind.ExtensionReceiver }
                val regularParams = calledFun.parameters - extensionParam
                arguments[0] = extensionParam?.let(call.arguments::get) ?: getMokkeryGlobalScope()
                arguments[1] = call.arguments[regularParams[0]!!] ?: irGetEnumEntry(
                    getClass(MokkeryIr.Class.MockMode),
                    mockMode.toString()
                )
                arguments[2] = call.arguments[regularParams[1]!!] ?: irNull()
                val anyType = context.irBuiltIns.anyType
                call.typeArguments
                    .filterNotNull()
                    .forEachIndexedTypeArgument { index, it ->
                        arguments[3 + index] = kClassReference(it ?: anyType)
                    }
            }
        }
    }

    private fun replaceWithSpy(call: IrCall): IrExpression {
        val typeToMock = call.typeArguments.firstOrNull() ?: return call
        val klass = typeToMock.getClass() ?: return call
        if (platform.isJs() && klass.defaultType.isAnyFunction()) return buildMockJsFunction(call, Spy)
        val spiedClass = spyCache.getOrPut(klass) { buildMockClass(Spy, klass) }
        return declarationIrBuilder {
            irCallConstructor(spiedClass.primaryConstructor!!) {
                val calledFun = call.symbol.owner
                val extensionParam = calledFun.parameters.find { it.kind == IrParameterKind.ExtensionReceiver }
                val regularParams = calledFun.parameters - extensionParam
                arguments[0] = extensionParam?.let(call.arguments::get) ?: getMokkeryGlobalScope()
                arguments[1] = irNull()
                arguments[2] = call.arguments[regularParams[1]!!] ?: irNull()
                arguments[3] = call.arguments[regularParams[0]!!]
                val anyType = context.irBuiltIns.anyType
                typeToMock.forEachIndexedTypeArgument { index, it ->
                    arguments[4 + index] = kClassReference(it ?: anyType)
                }
            }
        }
    }

    private fun replaceWithInternalEvery(
        expression: IrCall,
        function: IrSimpleFunctionSymbol
    ) = declarationIrBuilder {
        irBlock {
            +irCall(function) {
                val templatingArgument = expression.arguments[0]
                arguments[0] = when (templatingArgument) {
                    is IrFunctionExpression -> transformTemplatingBlock(templatingArgument.function)
                    is IrFunctionReference -> createTemplatingBlockForReference(expression, templatingArgument)
                    else -> error("Unsupported templating argument!")
                }
                typeArguments[0] = expression.typeArguments[0]
            }
        }
    }

    private fun replaceWithInternalVerify(expression: IrCall, function: IrSimpleFunctionSymbol): IrExpression {
        val mokkeryScopeParam = expression.symbol.owner
            .parameters
            .find { it.kind == IrParameterKind.ExtensionReceiver }
        val regularParams = expression.symbol.owner.parameters - mokkeryScopeParam
        val mode = expression.arguments[regularParams[0]!!]
        val block = expression.arguments[regularParams[1]!!]!!
        block as IrFunctionExpression
        return declarationIrBuilder {
            irBlock {
                +irCall(function) {
                    arguments[0] = mokkeryScopeParam
                        ?.let(expression.arguments::get)
                        ?: getMokkeryGlobalScope()
                    arguments[1] = mode ?: irGetVerifyMode(verifyMode)
                    arguments[2] = transformTemplatingBlock(block.function)
                }
            }
        }
    }

    private fun IrBuilderWithScope.irGetVerifyMode(verifyMode: VerifyMode): IrExpression {
        val expression = when (verifyMode) {
            is Soft -> irCallConstructor(getVerifyModeIrClass(verifyMode).primaryConstructor!!) {
                arguments[0] = irInt(verifyMode.atLeast)
                arguments[1] = irInt(verifyMode.atMost)
            }
            else -> irGetObject(getVerifyModeIrClass(verifyMode).symbol)
        }
        return expression
    }

    private fun getVerifyModeIrClass(verifyMode: VerifyMode): IrClass {
        val simpleName = verifyMode::class.simpleName
        return getIrClassOf(VerifyModeInternals::class)
            .nestedClasses
            .find { it.name.asString() == simpleName }!!
    }

    // probably works only for top level classes
    private fun getIrClassOf(cls: KClass<*>) = pluginContext
        .referenceClass(ClassId.fromString(cls.qualifiedName!!.replace(".", "/")))!!
        .owner

    private fun IrBlockBuilder.transformTemplatingBlock(function: IrSimpleFunction): IrFunctionExpression {
        val builtIns = pluginContext.irBuiltIns
        val lambdaType = pluginContext
            .irBuiltIns
            .let { if (function.isSuspend) it.suspendFunctionN(1) else it.functionN(1) }
            .typeWith(listOf(getClass(MokkeryIr.Class.MokkeryTemplatingScope).defaultType, builtIns.unitType))
        return irLambdaOf(lambdaType) { func ->
            val matchersInliningTransformer = MatchersInliningTransformer(
                compilerPluginScope = this@MokkeryTransformer,
                compileIfMatcher = matchersCompiler::compileIfMatcher,
                initialValueDeclarations = emptyList()
            )
            val templatingTransformer = TemplatingTransformer(
                compilerPluginScope = this@MokkeryTransformer,
                templatingScopeParam = func.parameters[0],
            )
            val templatingCleanupTransformer = TemplatingCleanupTransformer(this@MokkeryTransformer, function.symbol)
            val newBody = function
                .transform(matchersInliningTransformer, null)
                .transform(templatingTransformer, null)
                .transform(templatingCleanupTransformer, null)
                .let { it as IrFunction }
                .moveBodyTo(func, mapOf(function.parameters[0] to func.parameters[0]))
            newBody?.statements?.unaryPlus()
        }
    }

    private fun IrBuilderWithScope.getMokkeryGlobalScope() = mokkeryScopeCompanion
        .getProperty("global")
        .getter!!
        .let { irCall(it) { arguments[0] = irGetObject(mokkeryScopeCompanion.symbol) } }

    private fun IrBuilderWithScope.createTemplatingBlockForReference(
        everyCall: IrCall,
        functionReference: IrFunctionReference,
    ): IrFunctionExpression {
        val (mockObj, function) = functionReference.extractMockObjectAndFunction()
        val builtIns = pluginContext.irBuiltIns
        val lambdaType = pluginContext
            .irBuiltIns
            .let { if (function.isSuspend) it.suspendFunctionN(1) else it.functionN(1) }
            .typeWith(listOf(getClass(MokkeryIr.Class.MokkeryTemplatingScope).defaultType, builtIns.unitType))
        val runTemplateFun = when {
            function.isSuspend -> getFunction(MokkeryIr.Function.runTemplateSuspend)
            else -> getFunction(MokkeryIr.Function.runTemplate)
        }
        return irLambdaOf(lambdaType) { func ->
            +irCall(runTemplateFun) {
                typeArguments[0] = everyCall.typeArguments[0]
                arguments[0] = irGet(func.parameters[0])
                arguments[1] = mockObj
                arguments[2] = kClassReference(function.parentAsClass.defaultTypeErased)
                arguments[3] = irString(function.name.asString())
                arguments[4] = irLambdaOf(runTemplateFun.parameters[4].type.makeNotNull()) {
                    val typeParameters = mockObj
                        .type
                        .classOrFail
                        .owner
                        .typeParameters
                    +irReturn(irCallMapOfTemplatingParameters(function, typeParameters))
                }
                arguments[5] = irNull()
            }
        }

    }

    private fun IrBuilderWithScope.irCallMapOfTemplatingParameters(
        function: IrSimpleFunction,
        parentClassTypeParameters: List<IrTypeParameter>,
    ): IrCall {
        val templatingParameter = getClass(MokkeryIr.Class.TemplatingParameter)
        val argMatcherClass = getClass(MokkeryIr.Class.ArgMatcher)
        val anyMatcherObject = argMatcherClass
            .nestedClasses
            .single { it.name.asString() == "Any" }
        val templatingParameterConstructor = templatingParameter.primaryConstructor!!
        return irCallMapOf(
            transformer = this@MokkeryTransformer,
            pairs = function.nonDispatchParameters.memoryOptimizedMap {
                val param = irCallConstructor(templatingParameterConstructor) {
                    arguments[0] = irString(it.name.asString())
                    arguments[1] = irBoolean(it.isVararg)
                    val typeParam = it.type.asTypeParamOrNull()
                    if (typeParam in parentClassTypeParameters) {
                        arguments[3] = irInt(typeParam!!.index)
                    } else {
                        arguments[2] = kClassReference(it.type.eraseTypeParameters())
                    }
                }
                param to irGetObject(anyMatcherObject.symbol)
            },
            keyType = templatingParameter.defaultType,
            valueType = argMatcherClass.typeWith(context.irBuiltIns.anyNType)
        )
    }

    private fun IrFunctionReference.extractMockObjectAndFunction(): Pair<IrExpression, IrSimpleFunction> {
        // it handles 2 cases:
        // * this::property::get/this::property::set
        // * this::function
        return when (val dispatchArgument = arguments[0]!!) {
            is IrPropertyReference -> dispatchArgument.arguments[0]!! to when {
                symbol.owner.name.asString() == "get" -> dispatchArgument.getter!!.owner
                symbol.owner.name.asString() == "set" -> dispatchArgument.setter!!.owner
                else -> error("Only reference to getter and setter is allowed on property!")
            }
            else -> dispatchArgument to symbol.owner as IrSimpleFunction
        }
    }
}

