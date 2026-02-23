package dev.mokkery.plugin.ir.transformer.mock.stubs

import dev.mokkery.plugin.ir.irBuiltIns
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irFactory
import dev.mokkery.plugin.ir.overrideAllOverridableFunctions
import dev.mokkery.plugin.ir.overrideAllOverridableProperties
import dev.mokkery.plugin.ir.pluginContext
import dev.mokkery.plugin.ir.transformer.core.declarationIrBuilder
import dev.mokkery.plugin.randomString
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.parent
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.typeWithParameters
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.copyTypeParametersFrom
import org.jetbrains.kotlin.ir.util.createThisReceiverParameter
import org.jetbrains.kotlin.ir.util.defaultConstructor
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.findDeclaration
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.name.Name

class OverridableTypeStubStrategy(
    private val allowClassInheritance: Boolean,
) : StubStrategy {

    context(scope: StubStrategyScope)
    override fun provide(type: IrType): Stub? {
        val cls = type.classIfOverridableOrNull() ?: return null
        val baseClass = when {
            cls.isInterface -> irBuiltIns.anyClass.owner
            allowClassInheritance -> cls
            else -> return null
        }
        val file = scope.builder.parent.findCurrentIrFile()!!
        val stubNameBase = cls.createStubNameBase()
        val existingStub = file.findClassByNameBase(stubNameBase)
        if (existingStub != null) return stub { scope.builder.irCallConstructor(existingStub.primaryConstructor!!) }
        val constructorWithStubs = baseClass
            .defaultConstructor
            ?.takeIf { it.visibility in acceptedVisibilities }
            ?.let { it to emptyList() }
            ?: strategy.provideConstructorWithStubs(baseClass, acceptedVisibilities)
            ?: return null
        return stub {
            val cls = file
                .findClassByNameBase(stubNameBase)
                ?: file.createStubClass(Name.identifier("$stubNameBase$${randomString()}"), cls, baseClass, constructorWithStubs)
            scope.builder.irCallConstructor(cls.primaryConstructor!!)
        }
    }


    context(scope: StubStrategyScope)
    private fun IrFile.createStubClass(
        name: Name,
        original: IrClass,
        baseClass: IrClass,
        constructorWithStubs: Pair<IrConstructor, List<Stub>>
    ): IrClass {
        val cls = irFactory.buildClass { this.name = name }
        this.addChild(cls)
        cls.createThisReceiverParameter()
        cls.copyTypeParametersFrom(original)
        cls.superTypes = when {
            original.isInterface -> listOf(original.symbol.typeWithParameters(cls.typeParameters), baseClass.defaultType)
            else -> listOf(baseClass.symbol.typeWithParameters(cls.typeParameters))
        }
        cls.addConstructor { isPrimary = true }
            .apply {
                body = symbol.declarationIrBuilder.irBlockBody {
                    +irDelegatingConstructorWithStubs(constructorWithStubs)
                }
            }
        cls.overrideAllOverridableFunctions(pluginContext, original) { stubFunctionBody(it) }
        cls.overrideAllOverridableProperties(
            context = pluginContext,
            superClass = original,
            getterBlock = { stubFunctionBody(it) },
            setterBlock = { stubFunctionBody(it) }
        )
        return cls
    }


    private fun IrType.classIfOverridableOrNull(): IrClass? {
        val cls = classOrNull?.owner ?: return null
        if (cls.modality == Modality.SEALED) return null
        if (cls.modality == Modality.FINAL) return null
        return cls
    }

    private fun IrDeclarationParent.findCurrentIrFile(): IrFile? {
        return (this as? IrFile) ?: (this as? IrDeclaration)?.parent?.findCurrentIrFile()
    }

    private fun IrClass.createStubNameBase() = name.asString() + "$" + "Stub"

    private fun IrFile.findClassByNameBase(nameBase: String): IrClass? = findDeclaration<IrClass> {
        it.name.asString().contains(nameBase)
    }

    companion object {
        val acceptedVisibilities = setOf(
            DescriptorVisibilities.INTERNAL,
            DescriptorVisibilities.PUBLIC
        )
    }
}

