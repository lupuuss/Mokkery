package dev.mokkery.plugin.ir.transformer.templating.signature

import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.substitute
import dev.mokkery.plugin.ir.transformer.templating.matcher.templatingSubstitutionMap

class CompiledSignature(
    val returnType: IrType,
    val parameters: Map<IrValueParameter, CompiledParameter>,
)

class CompiledParameter(val type: IrType, val varargElementType: IrType?)

fun IrFunction.retypeTo(signature: CompiledSignature) {
    signature.parameters.forEach { (param, compiled) ->
        param.type = compiled.type
        param.varargElementType = compiled.varargElementType
    }
    returnType = signature.returnType
}

fun IrCall.retypeTo(signature: CompiledSignature) {
    val substitution = templatingSubstitutionMap
    type = signature.returnType.substitute(substitution)
    signature.parameters.forEach { (param, compiled) ->
        val arg = arguments[param] as? IrVararg ?: return@forEach
        arg.type = compiled.type.substitute(substitution)
        arg.varargElementType = compiled.varargElementType!!.substitute(substitution)
    }
}
