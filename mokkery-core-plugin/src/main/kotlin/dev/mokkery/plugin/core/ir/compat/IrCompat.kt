package dev.mokkery.plugin.core.ir.compat

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId

fun IrPluginContext.referenceFunctionsCompat(id: CallableId): Collection<IrSimpleFunctionSymbol> = try {
    this.finderForBuiltins().findFunctions(id)
} catch (_: NoSuchMethodError) {
    @Suppress("DEPRECATION")
    this.referenceFunctions(id)
}

fun IrPluginContext.referenceClassCompat(id: ClassId): IrClassSymbol? = try {
    this.finderForBuiltins().findClass(id)
} catch (_: NoSuchMethodError) {
    @Suppress("DEPRECATION")
    this.referenceClass(id)
}


fun IrPluginContext.referencePropertiesCompat(id: CallableId): Collection<IrPropertySymbol> = try {
    this.finderForBuiltins().findProperties(id)
} catch (_: NoSuchMethodError) {
    @Suppress("DEPRECATION")
    this.referenceProperties(id)
}
