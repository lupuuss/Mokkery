package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.CompilerPluginScope
import dev.mokkery.plugin.core.CoreTransformer
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.declarationIrBuilder
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getProperty
import dev.mokkery.plugin.core.mokkeryErrorAt
import dev.mokkery.plugin.ir.copyParametersWithoutDefaultsFrom
import org.jetbrains.kotlin.backend.common.ir.moveBodyTo
import org.jetbrains.kotlin.backend.common.lower.VariableRemapper
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.IrTypeParameterRemapper
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.copyTypeParametersFrom
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.nestedClasses
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.ir.util.remapTypes
import org.jetbrains.kotlin.name.Name

class MatchersCompiler(
    compilerPluginScope: CompilerPluginScope
) : CoreTransformer(compilerPluginScope) {
    private val argMatcherClass = getClass(Mokkery.Class.ArgMatcher)
    private val argMatcherCompositeClass = argMatcherClass.nestedClasses.single { it.name.asString() == "Composite" }
    private val varargMatcherClass = getClass(Mokkery.Class.VarArgMatcher)
    private val erasedMatcherCodeGetter = getProperty(Mokkery.Property.erasedMatcherCode).getter!!

    private val matcherAnnotation = getClass(Mokkery.Class.Matcher).symbol
    private val varargMatcherBuilderAnnotation = getClass(Mokkery.Class.VarArgMatcherBuilder).symbol
    private val argMatchersScopeType = getClass(Mokkery.Class.ArgMatchersScope).defaultType

    private val _compiled = mutableMapOf<IrFunctionSymbol, IrSimpleFunctionSymbol>()

    fun getCompiledFor(function: IrFunction): IrSimpleFunctionSymbol? {
        val matcher = _compiled[function.symbol]
        if (matcher != null) return matcher
        if (function.body == null) return null
        visitFunction(function)
        return _compiled[function.symbol]
    }

    override fun visitFunction(declaration: IrFunction): IrStatement {
        if (declaration.origin != IrDeclarationOrigin.DEFINED) return declaration
        if (declaration.parameters.none { it.type == argMatchersScopeType }) return super.visitFunction(declaration)
        if (_compiled.contains(declaration.symbol)) return super.visitFunction(declaration)
        if ((declaration as IrSimpleFunction).modality != Modality.FINAL) {
            mokkeryErrorAt(declaration, "Matcher must be a final function!")
        }
        pluginContext.irFactory.buildFun {
            updateFrom(declaration)
            startOffset = UNDEFINED_OFFSET
            endOffset = UNDEFINED_OFFSET
            name = matcherBackendName(declaration)
            visibility = declaration.visibility.coerceAtMost(DescriptorVisibilities.INTERNAL)
            origin = Mokkery.Origin
        }.apply {
            (declaration.parent as IrDeclarationContainer).addChild(this)
            copyTypeParametersFrom(declaration)
            val typeParametersMap = declaration.typeParameters.zip(typeParameters).toMap()
            val typesRemapper = IrTypeParameterRemapper(typeParametersMap)
            copyParametersWithoutDefaultsFrom(declaration, typeParametersMap)
            val anyCompositeParams = transformCompositeParamsTypes()
            _compiled[declaration.symbol] = this.symbol
            val inliner = MatchersInliner(
                compilerPluginScope = this@MatchersCompiler,
                argMatchersScopeParam = parameters.first { it.type == argMatchersScopeType },
                compiledMatchersLookUp = this@MatchersCompiler::getCompiledFor
            )
            inliner.currentFile = currentFile
            copyTransformedDefaultValuesFrom(declaration, inliner, typesRemapper)
            val matcherType = when {
                anyCompositeParams -> argMatcherCompositeClass
                declaration.hasAnnotation(varargMatcherBuilderAnnotation) -> varargMatcherClass
                else -> argMatcherClass
            }
            returnType = matcherType.typeWith(typesRemapper.remapType(declaration.returnType))
            body = declaration
                .moveBodyTo(this)!!
                .apply { remapTypes(typesRemapper) }
                .transform(inliner, null)
                .patchDeclarationParents(this)
        }
        declaration.eraseFrontMatcherBody()
        return declaration
    }

    private fun IrFunction.eraseFrontMatcherBody() {
        body = declarationIrBuilder(symbol) {
            irBlockBody {
                +irReturn(irCall(erasedMatcherCodeGetter))
            }
        }
    }

    private fun IrFunction.transformCompositeParamsTypes(): Boolean {
        var composite = false
        parameters.forEachIndexed { i, it ->
            if (it.hasAnnotation(matcherAnnotation)) {
                val matcherType = argMatcherClass.typeWith(it.type)
                if (it.isVararg) {
                    it.type = pluginContext.irBuiltIns.arrayClass.typeWith(matcherType)
                    it.varargElementType = matcherType
                } else {
                    it.type = matcherType
                }
                composite = true
            }
        }
        return composite
    }

    private fun IrFunction.copyTransformedDefaultValuesFrom(
        originalFunction: IrFunction,
        inliner: MatchersInliner,
        typesRemapper: IrTypeParameterRemapper
    ) {
        val variableRemapper = VariableRemapper(originalFunction.parameters.zip(parameters).toMap())
        parameters.forEachIndexed { i, it ->
            val originalParam = originalFunction.parameters[i]
            if (originalParam.hasAnnotation(matcherAnnotation)) {
                it.defaultValue = originalParam.defaultValue
                    ?.deepCopyWithSymbols(this)
                    ?.transform(variableRemapper, null)
                    ?.apply { remapTypes(typesRemapper) }
                    ?.transform(inliner, null)
            } else {
                it.defaultValue = originalParam.defaultValue
                    ?.deepCopyWithSymbols(this)
                    ?.transform(variableRemapper, null)
                    ?.apply { remapTypes(typesRemapper) }
            }
        }
    }

    private fun DescriptorVisibility.coerceAtMost(max: DescriptorVisibility): DescriptorVisibility {
        val result = this.compareTo(max)
        return when {
            result == null -> this
            result > 0 -> max
            else -> this
        }
    }

    companion object {

        fun matcherBackendName(function: IrFunction): Name = Name.identifier("_${function.name.asString()}MokkeryMatcher")
    }
}
