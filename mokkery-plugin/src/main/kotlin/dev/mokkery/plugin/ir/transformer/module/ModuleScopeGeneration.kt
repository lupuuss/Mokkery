package dev.mokkery.plugin.ir.transformer.module

import dev.mokkery.plugin.core.cacheKey
import dev.mokkery.plugin.core.caches
import dev.mokkery.plugin.core.context.configuration
import dev.mokkery.plugin.core.getOrPut
import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.irFactory
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.addToCurrentFile
import dev.mokkery.plugin.core.ir.transformer.currentFileValue
import dev.mokkery.plugin.core.ir.transformer.declarationIrBuilder
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.referencedCompanion
import dev.mokkery.plugin.core.ir.transformer.referencedDefaultType
import dev.mokkery.plugin.core.ir.transformer.referencedSymbol
import dev.mokkery.plugin.core.ir.transformer.replaceDeclarationIrBuilder
import dev.mokkery.plugin.defaultMockMode
import dev.mokkery.plugin.defaultVerifyMode
import dev.mokkery.plugin.ir.KotlinIr
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irGetEnumEntry
import dev.mokkery.plugin.ir.irLambdaOf
import dev.mokkery.plugin.ir.requirePropertyGetterOwner
import dev.mokkery.plugin.ir.transformer.core.irGetMokkeryModuleScope
import dev.mokkery.plugin.moduleScopeName
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verify.VerifyModeInternals.Soft
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.moduleName
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isMarkedNullable
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.findDeclaration
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.nestedClasses
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.name.Name
import java.io.File

private val moduleScopeAccessorCache by cacheKey<Unit, IrSimpleFunction>()

context(scope: TransformerScope)
val moduleScopePropertyAccessor: IrSimpleFunction
    get() = caches[moduleScopeAccessorCache].getOrPut(Unit) {
        currentFileValue
            .module
            .files
            .firstNotNullOfOrNull { file -> file.findDeclaration<IrProperty> { it.isModuleScope } }
            ?.getter
            ?: error("Module scope declaration could not be found!")
    }

context(scope: TransformerScope)
private val IrProperty.isModuleScope: Boolean
    get() = origin == MokkeryIr.Origin && name == configuration.moduleScopeName

context(scope: TransformerScope)
fun IrSimpleFunction.generateBodyIfModuleScopeGetter() {
    val property = correspondingPropertySymbol?.owner?.takeIf { it.isModuleScope } ?: return
    val scopeType = returnType
    body = symbol.declarationIrBuilder {
        val field = buildModuleScopeFieldInCurrentFile(property.name, scopeType)
        val valueGetter = referenced(KotlinIr.Class.Lazy).requirePropertyGetterOwner("value")
        irBlockBody {
            +irReturn(irCall(valueGetter, scopeType) { arguments[0] = irGetField(null, field) })
        }
    }
}

context(scope: TransformerScope)
private fun buildModuleScopeFieldInCurrentFile(propertyName: Name, scopeType: IrType): IrField {
    val field = irFactory.buildField {
        name = Name.identifier("_$propertyName")
        visibility = DescriptorVisibilities.PRIVATE
        isFinal = true
        isStatic = true
        origin = MokkeryIr.Origin
        type = referenced(KotlinIr.Class.Lazy).typeWith(scopeType)
    }
    field.addToCurrentFile()
    val initializers = collectModuleInitializers()
    recordUsageToIC(initializers)
    field.initializer = field.symbol.declarationIrBuilder {
        irExprBody(
            irCall(referenced(MokkeryIr.Function.moduleScopeLazy)) {
                arguments[0] = irLambdaOf(irBuiltIns.functionN(0).typeWith(scopeType)) {
                    +irReturn(irCreateModuleScope(initializers))
                }
            }
        )
    }
    return field
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.irCreateModuleScope(
    initializers: List<IrSimpleFunction>,
) = irCall(referenced(MokkeryIr.Function.createModuleScope)) {
    arguments[0] = irGetMokkeryScopeGlobal()
    arguments[1] = configuration.moduleName?.let(::irString) ?: irNull()
    arguments[2] = irGetEnumEntry(referenced(MokkeryIr.Class.MockMode), configuration.defaultMockMode)
    arguments[3] = irGetVerifyMode(configuration.defaultVerifyMode)
    arguments[4] = irInitializersLambda(initializers)
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.irInitializersLambda(initializers: List<IrSimpleFunction>): IrFunctionExpression {
    val lambdaType = irBuiltIns
        .functionN(1)
        .typeWith(referencedDefaultType(MokkeryIr.Class.MokkeryConfigurer), irBuiltIns.unitType)
    return irLambdaOf(lambdaType) { lambda ->
        val configurer = lambda.parameters.single()
        initializers.forEach { initializer ->
            +irCall(referenced(MokkeryIr.Function.runModuleInitializer)) {
                arguments[0] = irString(initializer.kotlinFqName.asString())
                arguments[1] = configuration.moduleName?.let(::irString) ?: irNull()
                arguments[2] = irLambdaOf(irBuiltIns.functionN(0).typeWith(irBuiltIns.unitType)) {
                    +irCall(initializer) {
                        if (initializer.parameters.isNotEmpty()) arguments[0] = irGet(configurer)
                    }
                }
            }
        }
    }
}

context(scope: TransformerScope)
private fun recordUsageToIC(declarations: Collection<IrDeclaration>) {
    val tracker = configuration[CommonConfigurationKeys.FILE_MAPPING_TRACKER] ?: return
    declarations
        .mapTo(mutableSetOf()) { declaration -> File(declaration.file.path) }
        .forEach(tracker::recordSourceReferencedByCompilerPlugin)
}

context(scope: TransformerScope)
private fun collectModuleInitializers(): List<IrSimpleFunction> {
    val annotationSymbol = referencedSymbol(MokkeryIr.Class.MokkeryModuleInit)
    val parameterSymbols = setOf(
        referencedSymbol(MokkeryIr.Class.MokkeryConfigurer),
        referencedSymbol(MokkeryIr.Class.MokkeryScope),
    )
    return currentFileValue
        .module
        .files
        .asSequence()
        .flatMap(IrFile::declarations)
        .filterIsInstance<IrSimpleFunction>()
        .filter { it.isMokkeryInitFunction(annotationSymbol, parameterSymbols) }
        .toList()
}

private fun IrSimpleFunction.isMokkeryInitFunction(
    annotationSymbol: IrClassSymbol,
    parameterSymbols: Set<IrClassSymbol>,
): Boolean {
    if (!hasAnnotation(annotationSymbol)) return false
    if (isSuspend || visibility == DescriptorVisibilities.PRIVATE || typeParameters.isNotEmpty()) return false
    if (parameters.isEmpty()) return true
    val type = parameters.singleOrNull()?.type ?: return false
    return !type.isMarkedNullable() && type.classOrNull in parameterSymbols
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

context(scope: TransformerScope)
fun IrCall.replaceModuleScope(): IrExpression = replaceDeclarationIrBuilder { irGetMokkeryModuleScope() }
