package dev.mokkery.plugin.core.ir.transformer

import dev.mokkery.plugin.core.caches
import dev.mokkery.plugin.core.getOrPut
import dev.mokkery.plugin.core.ir.Caches
import dev.mokkery.plugin.core.ir.IrClassReferencer
import dev.mokkery.plugin.core.ir.IrFunctionReferencer
import dev.mokkery.plugin.core.ir.IrPropertyReferencer
import dev.mokkery.plugin.core.ir.pluginContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
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
fun referencedGetter(resolver: IrPropertyReferencer): IrSimpleFunction = referenced(resolver).getter!!

context(scope: TransformerScope)
fun referencedGetterSymbol(resolver: IrPropertyReferencer): IrSimpleFunctionSymbol = referencedGetter(resolver).symbol

context(scope: TransformerScope)
fun referencedCompanion(resolver: IrClassReferencer): IrClass = referenced(resolver).companionObject()!!

context(scope: TransformerScope)
fun referencedPrimaryConstructor(resolver: IrClassReferencer): IrConstructor = referenced(resolver).primaryConstructor!!

context(scope: TransformerScope)
fun referencedDefaultType(resolver: IrClassReferencer): IrType = referenced(resolver).defaultType

context(scope: TransformerScope)
fun referencedSymbol(resolver: IrClassReferencer): IrClassSymbol = referenced(resolver).symbol
