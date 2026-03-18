package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.util.isOverridable

val IrProperty.isOverridable: Boolean
    get() = (getter?.isOverridable ?: true) && (setter?.isOverridable ?: true)
