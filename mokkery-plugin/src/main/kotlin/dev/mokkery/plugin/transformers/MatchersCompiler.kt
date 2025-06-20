package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.CompilerPluginScope
import dev.mokkery.plugin.core.CoreTransformer
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.getClass
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.irAttribute
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueParameterSymbol
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.eraseTypeParameters
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getArrayElementType
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.nestedClasses
import org.jetbrains.kotlin.ir.util.overrides
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.isSubpackageOf


var IrFunction.isCompiledMatcher: Boolean? by irAttribute(copyByDefault = true)

class MatchersCompiler(
    compilerPluginScope: CompilerPluginScope
) : CoreTransformer(compilerPluginScope) {

    private val argMatcherClass = getClass(Mokkery.Class.ArgMatcher)
    private val argMatcherCompositeClass = argMatcherClass.nestedClasses.single { it.name.asString() == "Composite" }
    private val varargMatcherClass = getClass(Mokkery.Class.VarArgMatcher)

    private val matcherAnnotation = getClass(Mokkery.Class.Matcher).symbol
    private val varargMatcherBuilderAnnotation = getClass(Mokkery.Class.VarArgMatcherBuilder).symbol
    private val argMatchersScopeType = getClass(Mokkery.Class.ArgMatchersScope).defaultType
    private val argMatchersScopeClass = getClass(Mokkery.Class.ArgMatchersScope)
    private val matchesFunction = argMatchersScopeClass.functions.first { it.name.asString() == "matches" }
    private val matchesCompositeFunction = argMatchersScopeClass.functions.first { it.name.asString() == "matchesComposite" }

    private val coreMatchers = mutableMapOf<IrSimpleFunctionSymbol, IrSimpleFunctionSymbol>()

    fun compileIfMatcher(function: IrSimpleFunction): IrSimpleFunction {
        val result = function.isCompiledMatcher
        if (result != null) {
            return coreMatchers[function.symbol]?.owner ?: function
        }
        if (function.parameters.none { it.type == argMatchersScopeType }) {
            function.isCompiledMatcher = false
            return function
        }
        val backendName = matcherBackendName(function)
        val coreMatcherCandidates = when {
            !function.kotlinFqName.isSubpackageOf(Mokkery.dev_mokkery) -> emptyList()
            else -> pluginContext.referenceFunctions(CallableId(function.parent.kotlinFqName, backendName))
        }
        return when {
            function.overrides(matchesFunction) || function.overrides(matchesCompositeFunction) -> function.apply {
                isCompiledMatcher = true
            }
            coreMatcherCandidates.isNotEmpty() -> {
                val backendFunc = coreMatcherCandidates
                    .singleOrNull()
                    ?.owner
                    ?: coreMatcherCandidates.matchTo(function.parameters)!!.owner
                coreMatchers[function.symbol] = backendFunc.symbol
                function.isCompiledMatcher = true
                backendFunc.apply { isCompiledMatcher = true }
            }
            else -> function.apply {
                isCompiledMatcher = true
                transformSignature()
                transformBody()
            }
        }
    }

    override fun visitFunction(declaration: IrFunction): IrStatement {
        if (declaration !is IrSimpleFunction || declaration.origin != IrDeclarationOrigin.DEFINED) return declaration
        return compileIfMatcher(declaration)
    }

    private fun IrFunction.transformBody() {
        body = body?.let { body ->
            val inliner = MatchersInliner(
                compilerPluginScope = this@MatchersCompiler,
                compileIfMatcher = this@MatchersCompiler::compileIfMatcher,
                initialValueDeclarations = parameters.filter { it.hasAnnotation(matcherAnnotation) }
            )
            inliner.currentFile = currentFile
            body.transform(inliner, null)
        }
    }

    private fun IrFunction.transformSignature(): List<IrValueParameterSymbol> {
        val matcherParams = transformCompositeParamsTypes()
        val type = when {
            matcherParams.any() -> argMatcherCompositeClass.typeWith(returnType)
            hasAnnotation(varargMatcherBuilderAnnotation) -> varargMatcherClass.defaultType
            else -> argMatcherClass.typeWith(returnType)
        }
        returnType = type
        return matcherParams
    }

    private fun IrFunction.transformCompositeParamsTypes(): List<IrValueParameterSymbol> {
        val matcherParams = mutableListOf<IrValueParameterSymbol>()
        parameters.forEachIndexed { i, it ->
            if (it.hasAnnotation(matcherAnnotation)) {
                matcherParams += it.symbol
                if (it.isVararg) {
                    val irBuiltIns = pluginContext.irBuiltIns
                    val matcherType = argMatcherClass.typeWith(it.type.getArrayElementType(irBuiltIns))
                    it.type = irBuiltIns.arrayClass.typeWith(matcherType)
                    it.varargElementType = matcherType
                } else {
                    it.type = argMatcherClass.typeWith(it.type)
                }
            }
        }
        return matcherParams
    }

    private fun Collection<IrSimpleFunctionSymbol>.matchTo(frontParams: List<IrValueParameter>): IrSimpleFunctionSymbol? {
        return find { func ->
            val result = func.owner.parameters.size == frontParams.size && func.owner.parameters
                .zip(frontParams) { b, f ->
                    b.type.classOrNull == f.type.classOrNull || b.type.eraseTypeParameters() == argMatcherClass.typeWith(
                        f.type
                    ).eraseTypeParameters()
                }
                .all { it }
            result
        }
    }



    private fun matcherBackendName(function: IrFunction): Name {
        return Name.identifier("_${function.name.asString()}MokkeryMatcher")
    }
}
