@file:Suppress("NOTHING_TO_INLINE")

package dev.mokkery.plugin.core.ir.transformer

import dev.mokkery.plugin.core.ir.IrMokkeryPluginScope
import org.jetbrains.kotlin.backend.common.ScopeWithIr
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.util.addChild

interface TransformerScope : IrMokkeryPluginScope

context(scope: TransformerScope)
inline val currentFileValue: IrFile
    get() = transformer.currentFileValue

context(scope: TransformerScope)
inline val currentScopeValue: ScopeWithIr
    get() = transformer.currentScopeValue

context(scope: TransformerScope)
inline fun  IrDeclaration.addToCurrentFile() {
    currentFileValue.addChild(this)
}
