package dev.mokkery.plugin.ir.transformer.core

import dev.mokkery.plugin.core.context.configuration
import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.irFactory
import dev.mokkery.plugin.core.ir.pluginContext
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.addToCurrentFile
import dev.mokkery.plugin.core.ir.transformer.currentFileValue
import dev.mokkery.plugin.core.ir.transformer.declarationIrBuilder
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.referencedCompanion
import dev.mokkery.plugin.core.ir.transformer.referencedDefaultType
import dev.mokkery.plugin.defaultMockMode
import dev.mokkery.plugin.defaultVerifyMode
import dev.mokkery.plugin.ir.KotlinIr
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.addOverridingMethod
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irGetEnumEntry
import dev.mokkery.plugin.ir.irVararg
import dev.mokkery.plugin.ir.overridePropertyBackingField
import dev.mokkery.plugin.ir.requirePropertyGetterOwner
import dev.mokkery.plugin.ir.requirePropertyOwner
import dev.mokkery.plugin.ir.requireSimpleFunctionOwner
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verify.VerifyModeInternals.Soft
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irConcat
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrVarargElement
import org.jetbrains.kotlin.ir.expressions.addArgument
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeSystemContextImpl
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.createThisReceiverParameter
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.findDeclaration
import org.jetbrains.kotlin.ir.util.isSubtypeOf
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.nestedClasses
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.name.Name

context(scope: TransformerScope)
fun IrBuilder.irCallMapOf(
    pairs: List<Pair<IrExpression, IrExpression>>,
    keyType: IrType,
    valueType: IrType
) = irCall(referenced(KotlinIr.Function.mapOf)) {
    val varargs = irVararg(
        elementType = referenced(KotlinIr.Class.Pair).typeWith(keyType, valueType),
        elements = pairs.map { irCreatePair(it.first, it.second) }
    )
    typeArguments[0] = keyType
    typeArguments[1] = valueType
    arguments[0] = varargs
}

context(scope: TransformerScope)
fun IrBuilder.irCallListOfPairs(
    pairs: List<Pair<IrExpression, IrExpression>>,
    firstType: IrType,
    secondType: IrType
) = irCall(referenced(KotlinIr.Function.listOf)) {
    val pairType = referenced(KotlinIr.Class.Pair).typeWith(firstType, secondType)
    val varargs = irVararg(
        elementType = pairType,
        elements = pairs.map { irCreatePair(it.first, it.second) }
    )
    typeArguments[0] = pairType
    arguments[0] = varargs
}


context(scope: TransformerScope)
fun IrBuilder.irCallListOf(
    type: IrType,
    elements: List<IrVarargElement>
) = irCall(referenced(KotlinIr.Function.listOf)) {
    arguments[0] = irVararg(elementType = type, elements = elements)
    typeArguments[0] = type
}

context(scope: TransformerScope)
fun IrBuilder.irCreatePair(
    first: IrExpression,
    second: IrExpression
): IrExpression = irCall(referenced(KotlinIr.Function.to)) {
    typeArguments[0] = first.type
    typeArguments[1] = second.type
    arguments[0] = first
    arguments[1] = second
}

context(scope: TransformerScope)
inline fun findOrBuildClassInCurrentFile(
    nameBase: String,
    nameHashSource: List<IrClass> = emptyList(),
    builder: (Name) -> IrClass
): IrClass {
    val name = generatedClassNameInCurrentFile(nameBase, nameHashSource)
    return currentFileValue.findDeclaration<IrClass> { it.name == name } ?: builder(name)
}

context(scope: TransformerScope)
fun generatedClassNameInCurrentFile(base: String, implements: List<IrClass>): Name {
    val prefix = when {
        implements.size == 1 -> "${implements[0].name}_${base}"
        else -> base
    }
    val hash = implements
        .map { it.kotlinFqName.asString() }
        .plus(currentFileValue.kotlinFqNameStringWithName())
        .hexHashString()
    return Name.identifier("${prefix}_${hash}")
}

context(scope: TransformerScope)
fun IrBuilderWithScope.irGetMokkeryScopeFor(call: IrCall): IrExpression {
    val typeSystem = IrTypeSystemContextImpl(irBuiltIns)
    val mokkeryScope = referencedDefaultType(MokkeryIr.Class.MokkeryScope)
    val scopeParam = call.symbol.owner.parameters.find { it.type.isSubtypeOf(mokkeryScope, typeSystem) }
    if (scopeParam == null) return irGetMokkeryFileScope()
    return call.arguments[scopeParam]!!
}

context(scope: TransformerScope)
fun IrBuilderWithScope.irGetMokkeryFileScope(): IrExpression {
    val fileScopeObj = findOrBuildClassInCurrentFile("MokkeryFileScope") {
        buildFileScopeClass(it)
    }
    return irGetObject(fileScopeObj.symbol)
}

context(scope: TransformerScope)
private fun buildFileScopeClass(name: Name): IrClass {
    val createFileContextFun = referenced(MokkeryIr.Function.fileContext)
    val mockModeClass = referenced(MokkeryIr.Class.MockMode)
    val cls = irFactory.buildClass {
        this.name = name
        this.kind = ClassKind.OBJECT
        this.visibility = DescriptorVisibilities.PRIVATE
        this.origin = MokkeryIr.Origin
    }
    cls.addToCurrentFile()
    cls.createThisReceiverParameter()
    val scopeClass = referenced(MokkeryIr.Class.MokkeryScope)
    cls.superTypes = listOf(irBuiltIns.anyType, scopeClass.defaultType)
    val contextProperty = cls.overridePropertyBackingField(
        context = pluginContext,
        property = scopeClass.requirePropertyOwner("mokkeryContext")
    )
    val toStringFun = irBuiltIns.anyClass.owner.requireSimpleFunctionOwner("toString")
    cls.addOverridingMethod(pluginContext, toStringFun) {
        val concat = irConcat()
        concat.addArgument(irString("MokkeryScope(mokkeryContext="))
        concat.addArgument(
            irCall(toStringFun) {
                arguments[0] = irGetField(irGet(it.parameters[0]), contextProperty.backingField!!)
            }
        )
        concat.addArgument(irString(")"))
        +irReturn(concat)
    }
    cls.addConstructor {
        isPrimary = true
        origin = MokkeryIr.Origin
    }.apply {
        body = symbol.declarationIrBuilder.irBlockBody {
            +irDelegatingConstructorCall(irBuiltIns.anyClass.owner.primaryConstructor!!)
            +irSetField(
                receiver = irGet(cls.thisReceiver!!),
                field = contextProperty.backingField!!,
                value = irCall(createFileContextFun) {
                    arguments[0] = irGetMokkeryScopeGlobal()
                    arguments[1] = irGetEnumEntry(mockModeClass, configuration.defaultMockMode)
                    arguments[2] = irGetVerifyMode(configuration.defaultVerifyMode)
                }
            )
        }
    }
    return cls
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.irGetMokkeryScopeGlobal(): IrCall {
    val scopeCompanion = referencedCompanion(MokkeryIr.Class.MokkeryScope)
    return scopeCompanion
        .requirePropertyGetterOwner("global")
        .let { irCall(it) { arguments[0] = irGetObject(scopeCompanion.symbol) } }
}


context(scope: TransformerScope)
private fun IrBuilderWithScope.irGetVerifyMode(verifyMode: VerifyMode) = when (verifyMode) {
    is Soft -> irCallConstructor(verifyMode.toIrClass().primaryConstructor!!) {
        arguments[0] = irInt(verifyMode.atLeast)
        arguments[1] = irInt(verifyMode.atMost)
    }
    else -> irGetObject(verifyMode.toIrClass().symbol)
}

context(scope: TransformerScope)
private fun VerifyMode.toIrClass(): IrClass {
    val simpleName = this::class.simpleName
    return referenced(MokkeryIr.Class.VerifyModeInternals)
        .nestedClasses
        .find { it.name.asString() == simpleName }!!
}

private fun List<String>.hexHashString(): String {
    var hash = 0xcbf29ce484222325UL
    forEach {
        it.forEach { c -> hash = (hash xor c.code.toULong()) * 0x100000001b3UL }
    }
    return hash.toString(36)
}

private fun IrFile.kotlinFqNameStringWithName() = kotlinFqName.asString() + "." + this.name
