package dev.mokkery.plugin.ir.transformer.mock.stubs

import dev.mokkery.plugin.Kotlin
import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.ir.argumentTypes
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.typeArgumentsFrom
import dev.mokkery.plugin.ir.typeSubstitution
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.defaultConstructor
import org.jetbrains.kotlin.ir.util.isSubclassOf
import org.jetbrains.kotlin.ir.util.packageFqName
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.name.isSubpackageOf

class ConstructableClassStubStrategy(
    private val allowConcreteClassInstantiation: Boolean,
) : StubStrategy {

    context(scope: StubStrategyScope)
    override fun provide(type: IrType): Stub? {
        val cls = type.classOrNull?.owner ?: return null
        if (cls.modality == Modality.ABSTRACT || cls.modality == Modality.SEALED) return null
        val allowInstantiation = allowConcreteClassInstantiation
                // instantiation sometimes does not require opt-in
                || cls.isValue
                || cls.isSubclassOf(irBuiltIns.throwableClass.owner)
                || cls.packageFqName?.isSubpackageOf(Kotlin.kotlin_collections) == true
                || cls.packageFqName?.isSubpackageOf(Kotlin.kotlin_sequences) == true
                || cls.packageFqName?.isSubpackageOf(Kotlin.kotlin_ranges) == true
        if (!allowInstantiation) return null
        val defaultConstructor = cls.defaultConstructor
        if (defaultConstructor != null && defaultConstructor.visibility in acceptedVisibilities) return stub {
            scope.builder.irCallConstructor(defaultConstructor, type.toTypeArgumentsOf(defaultConstructor))
        }
        return strategy
            .provideConstructorWithStubs(cls, acceptedVisibilities, type.typeSubstitution)
            ?.let {
                stub { scope.builder.irCallConstructorWithStubs(it, type.toTypeArgumentsOf(it.first)) }
            }
    }

    private fun IrType.toTypeArgumentsOf(constructor: IrConstructor): List<IrType> {
        return constructor.parentAsClass.typeArgumentsFrom(argumentTypes) + constructor.typeArgumentsFrom(emptyList())
    }


    companion object {

        val acceptedVisibilities = setOf(DescriptorVisibilities.INTERNAL, DescriptorVisibilities.PUBLIC)
    }
}
