package dev.mokkery.plugin.ir.transformer.templating.matcher

import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.ir.MokkeryIr
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.getArrayElementType

context(scope: TransformerScope)
fun IrType.asMatcherType(): IrType = referenced(MokkeryIr.Class.ArgMatcher).typeWith(this)

context(scope: TransformerScope)
fun IrType.asMatchersArrayType(): IrType = irBuiltIns.arrayClass.typeWith(getArrayElementType(irBuiltIns).asMatcherType())
