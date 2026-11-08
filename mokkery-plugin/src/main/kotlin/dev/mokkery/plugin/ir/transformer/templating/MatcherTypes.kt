package dev.mokkery.plugin.ir.transformer.templating

import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.ir.MokkeryIr
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isArray
import org.jetbrains.kotlin.ir.types.isMarkedNullable
import org.jetbrains.kotlin.ir.util.getArrayElementType
import org.jetbrains.kotlin.ir.util.isPrimitiveArray
import org.jetbrains.kotlin.ir.util.isSubclassOf
import org.jetbrains.kotlin.ir.util.isUnsignedArray

context(scope: TransformerScope)
fun IrType.isMatcher(): Boolean = !isMarkedNullable() && isArgMatcherType()

context(scope: TransformerScope)
fun IrType.isMatchersArray(): Boolean = isArray() && getArrayElementType(irBuiltIns).isMatcher()

context(scope: TransformerScope)
fun IrType.isArgMatcherType(): Boolean {
    val argMatcherClass = referenced(MokkeryIr.Class.ArgMatcher)
    val clazz = classOrNull?.owner ?: return false
    return clazz == argMatcherClass || clazz.isSubclassOf(argMatcherClass)
}

context(scope: TransformerScope)
fun IrType.isArgMatcherValueType(): Boolean {
    return isArgMatcherType() || isArray() && getArrayElementType(irBuiltIns).isArgMatcherType()
}

fun IrType.isAnyArray(): Boolean = isArray() || isPrimitiveArray() || isUnsignedArray()
