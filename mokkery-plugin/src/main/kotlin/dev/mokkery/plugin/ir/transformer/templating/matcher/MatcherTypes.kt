package dev.mokkery.plugin.ir.transformer.templating.matcher

import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.erasedUpperBound
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.getArrayElementType
import org.jetbrains.kotlin.ir.util.typeSubstitutionMap

context(scope: TransformerScope)
fun IrType.asMatcherType(): IrType = referenced(MokkeryIr.Class.ArgMatcher).typeWith(this)

context(scope: TransformerScope)
fun IrType.asMatchersArrayType(): IrType = irBuiltIns.arrayClass.typeWith(getArrayElementType(irBuiltIns).asMatcherType())

val IrCall.templatingSubstitutionMap
    get() = (symbol.owner.parent as? IrClass)
        ?.typeParameters
        .orEmpty()
        .associate { it.symbol to it.erasedUpperBound }
        .plus(typeSubstitutionMap)
