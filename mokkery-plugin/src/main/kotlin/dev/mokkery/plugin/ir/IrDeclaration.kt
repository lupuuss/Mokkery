package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.util.RenderIrElementVisitor

fun IrDeclaration.renderSymbol() = RenderIrElementVisitor().renderSymbolReference(symbol)
