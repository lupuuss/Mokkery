package dev.mokkery.plugin.ir.transformer.core

import dev.mokkery.plugin.caches
import dev.mokkery.plugin.getOrPut
import dev.mokkery.plugin.ir.Caches
import dev.mokkery.plugin.ir.IrClassReferencer
import dev.mokkery.plugin.ir.IrFunctionReferencer
import dev.mokkery.plugin.ir.IrPropertyReferencer
import dev.mokkery.plugin.ir.pluginContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.primaryConstructor


context(scope: TransformerScope)
fun referenced(referencer: IrClassReferencer): IrClass = caches[Caches.classReferences]
    .getOrPut(referencer) {
        referencer.reference(pluginContext)
    }

context(scope: TransformerScope)
fun referenced(referencer: IrFunctionReferencer): IrSimpleFunction = caches[Caches.functionReferences]
    .getOrPut(referencer) {
        referencer.reference(pluginContext)
    }


context(scope: TransformerScope)
fun referenced(referencer: IrPropertyReferencer): IrProperty = caches[Caches.propertyReferences]
    .getOrPut(referencer) {
        referencer.reference(pluginContext)
    }

context(scope: TransformerScope)
fun referencedCompanion(resolver: IrClassReferencer): IrClass = referenced(resolver).companionObject()!!

context(scope: TransformerScope)
fun referencedPrimaryConstructor(resolver: IrClassReferencer): IrConstructor = referenced(resolver).primaryConstructor!!

context(scope: TransformerScope)
fun referencedDefaultType(resolver: IrClassReferencer): IrType = referenced(resolver).defaultType

context(scope: TransformerScope)
fun referencedSymbol(resolver: IrClassReferencer): IrClassSymbol = referenced(resolver).symbol
