@file:Suppress("NOTHING_TO_INLINE")

package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

context(transformer: IrElementTransformerVoid)
inline fun <T : IrElement> T.applyTransformChildrenVoid(): T {
    this.transformChildrenVoid(transformer)
    return this
}
