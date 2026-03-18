package dev.mokkery.plugin.ir.compat

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.declarations.IrFieldBuilder
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrProperty
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


fun IrProperty.addBackingFieldCompat(builder: IrFieldBuilder.() -> Unit): IrField {
    return factory.buildField {
        name = this@addBackingFieldCompat.name
        origin = IrDeclarationOrigin.PROPERTY_BACKING_FIELD_COMPAT
        visibility = DescriptorVisibilities.PRIVATE
        builder()
    }.also { field ->
        this@addBackingFieldCompat.backingField = field
        field.correspondingPropertySymbol = this@addBackingFieldCompat.symbol
        field.parent = this@addBackingFieldCompat.parent
    }
}

val IrDeclarationOrigin.Companion.LOCAL_FUNCTION_FOR_LAMBDA_COMPAT: IrDeclarationOrigin
    get() = try {
        IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
    } catch (_: NoSuchMethodError) {
        LOCAL_FUNCTION_FOR_LAMBDA_OLD
    }

val IrDeclarationOrigin.Companion.PROPERTY_BACKING_FIELD_COMPAT: IrDeclarationOrigin
    get() = try {
        IrDeclarationOrigin.PROPERTY_BACKING_FIELD
    } catch (_: NoSuchMethodError) {
        PROPERTY_BACKING_FIELD_OLD
    }

val IrDeclarationOrigin.Companion.DEFAULT_PROPERTY_ACCESSOR_COMPAT: IrDeclarationOrigin
    get() = try {
        IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
    } catch (_: NoSuchMethodError) {
        DEFAULT_PROPERTY_ACCESSOR_OLD
    }

val IrDeclarationOrigin.Companion.DEFINED_COMPAT: IrDeclarationOrigin
    get() = try {
        IrDeclarationOrigin.DEFINED
    } catch (_: NoSuchMethodError) {
        DEFINED_OLD
    }

private val DEFINED_OLD: IrDeclarationOrigin by lazy {
    IrDeclarationOrigin.Companion::class.java
        .methods
        .first { it.name == "getDEFINED" }
        .invoke(IrDeclarationOrigin.Companion) as IrDeclarationOrigin
}

private val LOCAL_FUNCTION_FOR_LAMBDA_OLD: IrDeclarationOrigin by lazy {
    IrDeclarationOrigin.Companion::class.java
        .methods
        .first { it.name == "getLOCAL_FUNCTION_FOR_LAMBDA" }
        .invoke(IrDeclarationOrigin.Companion) as IrDeclarationOrigin
}

private val DEFAULT_PROPERTY_ACCESSOR_OLD: IrDeclarationOrigin by lazy {
    IrDeclarationOrigin.Companion::class.java
        .methods
        .first { it.name == "getDEFAULT_PROPERTY_ACCESSOR" }
        .invoke(IrDeclarationOrigin.Companion) as IrDeclarationOrigin
}

private val PROPERTY_BACKING_FIELD_OLD: IrDeclarationOrigin by lazy {
    IrDeclarationOrigin.Companion::class.java
        .methods
        .first { it.name == "getPROPERTY_BACKING_FIELD" }
        .invoke(IrDeclarationOrigin.Companion) as IrDeclarationOrigin
}
