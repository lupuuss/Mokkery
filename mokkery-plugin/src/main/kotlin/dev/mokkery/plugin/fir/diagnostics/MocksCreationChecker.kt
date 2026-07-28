package dev.mokkery.plugin.fir.diagnostics

import dev.mokkery.plugin.Kotlin
import dev.mokkery.plugin.MembersValidationMode
import dev.mokkery.plugin.Mokkery.Callable
import dev.mokkery.plugin.core.fir.hasMokkeryGeneratedConstructor
import dev.mokkery.plugin.fir.declaredMembers
import dev.mokkery.plugin.stubsConfig
import dev.mokkery.plugin.validationMode
import org.jetbrains.kotlin.AbstractKtSourceElement
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.error2
import org.jetbrains.kotlin.diagnostics.error3
import org.jetbrains.kotlin.diagnostics.rendering.Renderer
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.analysis.checkers.isSubtypeOfThrowable
import org.jetbrains.kotlin.fir.declarations.constructors
import org.jetbrains.kotlin.fir.declarations.utils.isEnumClass
import org.jetbrains.kotlin.fir.declarations.utils.isFinal
import org.jetbrains.kotlin.fir.declarations.utils.isInline
import org.jetbrains.kotlin.fir.declarations.utils.isInlineOrValue
import org.jetbrains.kotlin.fir.declarations.utils.isInterface
import org.jetbrains.kotlin.fir.declarations.utils.isSealed
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.isPrimitiveType
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeTypeParameterType
import org.jetbrains.kotlin.fir.types.FirTypeProjection
import org.jetbrains.kotlin.fir.types.isSomeFunctionType
import org.jetbrains.kotlin.fir.types.toConeTypeProjection
import org.jetbrains.kotlin.fir.types.type
import org.jetbrains.kotlin.fir.types.typeContext
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.name.isSubpackageOf
import org.jetbrains.kotlin.platform.isJs
import org.jetbrains.kotlin.platform.isWasm
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.types.model.eraseContainingTypeParameters
import org.jetbrains.kotlin.types.model.isNullableType

class MocksCreationChecker(
    configuration: CompilerConfiguration,
) : FirFunctionCallChecker(MppCheckerKind.Common) {
    private val mock = Callable.mock
    private val mockMany = Callable.mockMany
    private val spy = Callable.spy


    private val validationMode = configuration.validationMode
    private val stubsConfig = configuration.stubsConfig
    private val wasmHashCode = Name.identifier("_hashCode")
    private val wasmTypeInfo = Name.identifier("typeInfo")
    private val wasmSpecialPropertyNames = listOf(wasmHashCode, wasmTypeInfo)


    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        val callee = expression.calleeReference as? FirResolvedNamedReference ?: return
        val symbol = callee.resolvedSymbol as? FirNamedFunctionSymbol ?: return
        context(symbol) {
            when (symbol.callableId) {
                mock, spy -> checkInterception(expression)
                mockMany -> checkManyInterceptions(expression)
            }
        }
    }

    context(context: CheckerContext, reporter: DiagnosticReporter, funSymbol: FirNamedFunctionSymbol)
    private fun checkManyInterceptions(expression: FirFunctionCall) {
        val classMappings = expression.typeArguments.groupBy {
            val type = it.toConeTypeProjection().type ?: return
            if (!checkInterceptionType(it.source ?: expression.source, type)) return
            val classSymbol = type.toRegularClassSymbol() ?: return
            classSymbol
        }
        if (!checkNoDuplicates(expression.typeArguments.size, classMappings)) return
        if (!checkOneSuperClass(classMappings)) return
        if (!checkJsFunctionalTypes(classMappings)) return
    }

    context(context: CheckerContext, reporter: DiagnosticReporter, funSymbol: FirNamedFunctionSymbol)
    private fun checkInterception(expression: FirFunctionCall): Boolean {
        val typeArg = expression.typeArguments.first()
        val type = typeArg.toConeTypeProjection().type ?: return false
        val source = typeArg.source ?: expression.source
        return checkInterceptionType(source, type)
    }

    context(context: CheckerContext, reporter: DiagnosticReporter, funSymbol: FirNamedFunctionSymbol)
    private fun checkNoDuplicates(
        argumentsCount: Int,
        classMapping: Map<FirRegularClassSymbol, List<FirTypeProjection>>
    ): Boolean {
        if (classMapping.size == argumentsCount) return true
        val entry = classMapping.entries.first { it.value.size > 1 }
        reporter.reportOn(
            source = entry.value[1].source,
            factory = Diagnostics.DUPLICATE_TYPES_FOR_MOCK_MANY,
            a = entry.key.defaultType(),
            b = funSymbol.name,
            c = entry.value.size.toString(),
        )
        return false
    }

    context(context: CheckerContext, reporter: DiagnosticReporter, funSymbol: FirNamedFunctionSymbol)
    private fun checkOneSuperClass(classMapping: Map<FirRegularClassSymbol, List<FirTypeProjection>>): Boolean {
        val regularClasses = classMapping.keys.filter { it.classKind == ClassKind.CLASS }
        if (regularClasses.size <= 1) return true
        reporter.reportOn(
            source = classMapping.getValue(regularClasses[1]).first().source,
            factory = Diagnostics.MULTIPLE_SUPER_CLASSES_FOR_MOCK_MANY,
            a = funSymbol.name,
            b = regularClasses.map { it.defaultType() },
        )
        return false
    }

    context(context: CheckerContext, reporter: DiagnosticReporter, funSymbol: FirNamedFunctionSymbol)
    private fun checkJsFunctionalTypes(classMapping: Map<FirRegularClassSymbol, List<FirTypeProjection>>): Boolean {
        if (!context.session.moduleData.platform.isJs()) return true
        val funClass = classMapping.keys.find { it.defaultType().isSomeFunctionType(context.session) } ?: return true
        reporter.reportOn(
            source = classMapping.getValue(funClass).first().source,
            factory = Diagnostics.FUNCTIONAL_TYPE_ON_JS_FOR_MOCK_MANY,
            a = classMapping.getValue(funClass).first().toConeTypeProjection().type!!,
            b = funSymbol.name,
        )
        return false
    }

    // checkInterception

    context(context: CheckerContext, reporter: DiagnosticReporter, funSymbol: FirNamedFunctionSymbol)
    private fun checkInterceptionType(source: AbstractKtSourceElement?, type: ConeKotlinType): Boolean {
        if (!checkInterceptionTypeParameter(source, type)) return false
        val classSymbol = type.toRegularClassSymbol() ?: return false
        if (!checkInterceptionModality(source, classSymbol)) return false
        if (classSymbol.isInterface) return true
        if (!checkClassInterceptionRequirements(source, classSymbol)) return false
        return true
    }

    context(context: CheckerContext, reporter: DiagnosticReporter, funSymbol: FirNamedFunctionSymbol)
    private fun checkInterceptionTypeParameter(source: AbstractKtSourceElement?, type: ConeKotlinType): Boolean {
        if (type !is ConeTypeParameterType) return true
        reporter.reportOn(
            source = source,
            factory = Diagnostics.INDIRECT_INTERCEPTION,
            a = funSymbol.name,
            b = type,
        )
        return false
    }

    context(context: CheckerContext, reporter: DiagnosticReporter, funSymbol: FirNamedFunctionSymbol)
    private fun checkInterceptionModality(
        source: AbstractKtSourceElement?,
        classSymbol: FirRegularClassSymbol
    ): Boolean {
        val modality = classSymbol.modality
        val modalityDiagnostic = when {
            classSymbol.isPrimitiveType() -> Diagnostics.PRIMITIVE_TYPE_CANNOT_BE_INTERCEPTED
            modality == Modality.SEALED -> Diagnostics.SEALED_TYPE_CANNOT_BE_INTERCEPTED
            modality == Modality.FINAL -> Diagnostics.FINAL_TYPE_CANNOT_BE_INTERCEPTED
            else -> null
        }
        if (modalityDiagnostic == null) return true
        reporter.reportOn(
            source = source,
            factory = modalityDiagnostic,
            a = funSymbol.name,
            b = classSymbol.defaultType(),
        )
        return false
    }

    context(context: CheckerContext, reporter: DiagnosticReporter, funSymbol: FirNamedFunctionSymbol)
    private fun checkClassInterceptionRequirements(
        source: AbstractKtSourceElement?,
        classSymbol: FirRegularClassSymbol
    ): Boolean {
        val constructors = classSymbol
            .constructors(context.session)
            .filter { it.isAccessible() }
        if (constructors.isEmpty()) {
            reporter.reportOn(
                source = source,
                factory = Diagnostics.NO_PUBLIC_CONSTRUCTOR_TYPE_CANNOT_BE_INTERCEPTED,
                a = funSymbol.name,
                b = classSymbol.defaultType(),
            )
            return false
        }
        checkConstructorPossibleToStub(source, classSymbol, constructors)
        val inheritedSymbols = classSymbol
            .resolvedSuperTypes
            .asSequence()
            .mapNotNull { it.toRegularClassSymbol() }
            .flatMap { it.declaredMembers(context.session) }
        val allDeclarationSymbols = classSymbol
            .declaredMembers(context.session)
            .asSequence()
            .plus(inheritedSymbols)
        val finalDeclarations = allDeclarationSymbols
            .filterOutWasmSpecialProperties() // TODO Remove when not detectable by FIR
            .filterNot { it.isValid(validationMode) }
            .toList()
        if (finalDeclarations.isEmpty()) return true
        reporter.reportOn(
            source = source,
            factory = Diagnostics.FINAL_MEMBERS_TYPE_CANNOT_BE_INTERCEPTED,
            a = funSymbol.name,
            b = classSymbol.defaultType(),
            c = finalDeclarations,
        )
        return false
    }

    private fun FirBasedSymbol<*>.isValid(validationMode: MembersValidationMode): Boolean {
        if (this !is FirCallableSymbol<*>) return true
        if (this is FirConstructorSymbol) return true
        if (visibility == Visibilities.Private) return true
        if (!isFinal) return true
        return when (validationMode) {
            MembersValidationMode.Strict -> false
            MembersValidationMode.IgnoreInline -> isInlineOrInlineProperty
            MembersValidationMode.IgnoreFinal -> true
        }
    }

    private val FirCallableSymbol<*>.isInlineOrInlineProperty: Boolean
        get() {
            if (this !is FirPropertySymbol) return isInline
            val getter = getterSymbol
            val setter = setterSymbol
            return (getter == null || getter.isInline) && (setter == null || setter.isInline)
        }

    context(context: CheckerContext)
    private fun Sequence<FirBasedSymbol<*>>.filterOutWasmSpecialProperties(): Sequence<FirBasedSymbol<*>> {
        if (!context.session.moduleData.platform.isWasm()) return this
        return filterNot { it is FirPropertySymbol && it.name in wasmSpecialPropertyNames }
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun checkConstructorPossibleToStub(
        source: AbstractKtSourceElement?,
        classSymbol: FirRegularClassSymbol,
        constructors: List<FirConstructorSymbol>
    ) {
        constructors.forEach { if (it.isPossibleToStub()) return }
        val problems = constructors.flatMap { ctor ->
            ctor.valueParameterSymbols.flatMap { param -> param.collectStubErrors() }
        }
        reporter.reportOn(
            source = source,
            factory = Diagnostics.NO_CONSTRUCTOR_TO_STUB,
            a = classSymbol.name,
            b = problems.distinctBy { it.first },
        )
    }

    context(context: CheckerContext)
    private fun FirValueParameterSymbol.collectStubErrors(
        typesStack: Set<ConeKotlinType> = emptySet()
    ): List<Pair<ConeKotlinType, StubError>> {
        if (hasDefaultValue) return emptyList()
        val type = context(context.session.typeContext) {
            this.resolvedReturnType.eraseContainingTypeParameters() as ConeKotlinType
        }
        if (type in typesStack) return listOf(type to StubError.Recursion)
        if (type.isPossibleToStubByDefault()) return emptyList()
        val cls = type
            .toRegularClassSymbol()
            ?: return listOf(type to StubError.NoAccessibleConstructors)
        val constructors = cls
            .constructors(context.session)
            .filter { it.isAccessible() }
        if (constructors.isEmpty()) return listOf(type to StubError.NoAccessibleConstructors)
        val problems = constructors.flatMap { ctor ->
            ctor.valueParameterSymbols.flatMap { param -> param.collectStubErrors(typesStack + type) }
        }
        if (problems.isNotEmpty()) return problems
        val isInstantiable = cls.isInstantiableClass(typesStack)
        val isOverridable = cls.isOverridableClass(typesStack)
        if (stubsConfig.allowConcreteClassInstantiation && isInstantiable) return emptyList()
        if (stubsConfig.allowClassInheritance && isOverridable) return emptyList()
        if (!stubsConfig.allowConcreteClassInstantiation && isInstantiable) {
            return listOf(type to StubError.ClassInstantiationNeedsOptIn)
        }
        if (!stubsConfig.allowClassInheritance && isOverridable) {
            return listOf(type to StubError.ClassOverrideNeedsOptIn)
        }
        return emptyList()
    }

    context(context: CheckerContext)
    private fun FirConstructorSymbol.isPossibleToStub(
        typesStack: Set<ConeKotlinType> = emptySet()
    ) = isAccessible() && valueParameterSymbols.all { it.resolvedReturnType.isPossibleToStub(typesStack) }

    context(context: CheckerContext)
    private fun ConeKotlinType.isPossibleToStub(typesStack: Set<ConeKotlinType> = emptySet()): Boolean {
        val erased = context(context.session.typeContext) {
            this.eraseContainingTypeParameters() as ConeKotlinType
        }
        if (erased in typesStack) return false
        if (erased.isPossibleToStubByDefault()) return true
        val cls = erased.toRegularClassSymbol() ?: return false
        return (stubsConfig.allowConcreteClassInstantiation && cls.isInstantiableClass(typesStack + this))
                || (stubsConfig.allowClassInheritance && cls.isOverridableClass(typesStack + this))
    }

    context(context: CheckerContext)
    private fun ConeKotlinType.isPossibleToStubByDefault() = context(context.session.typeContext) {
        isNullableType()
                || isAnyOf(defaultTypesToStub)
                || isSomeFunctionType(context.session)
                || toRegularClassSymbol()?.let { cls ->
                    val fqName = cls.packageFqName()
                    cls.isRegularInterface()
                            || cls.hasMokkeryGeneratedConstructor(context.session)
                            || cls.isEnumClass
                            || cls.isInlineClass()
                            || fqName.isSubpackageOf(Kotlin.kotlin_collections)
                            || fqName.isSubpackageOf(Kotlin.kotlin_ranges)
                            || fqName.isSubpackageOf(Kotlin.kotlin_sequences)
                } == true
                || isSubtypeOfThrowable(context.session)

    }

    private fun FirRegularClassSymbol?.isRegularInterface(): Boolean = this?.classKind == ClassKind.INTERFACE && !this.isSealed

    private fun FirRegularClassSymbol?.isInlineClass(): Boolean = this?.isInlineOrValue == true

    context(context: CheckerContext)
    private fun FirRegularClassSymbol.isOverridableClass(
        typesStack: Set<ConeKotlinType>
    ): Boolean = context(context.session.typeContext) {
        this.classKind == ClassKind.CLASS
                && this.isOverridable()
                && this.constructors(context.session).any { it.isPossibleToStub(typesStack) }
    }

    context(context: CheckerContext)
    private fun FirRegularClassSymbol.isInstantiableClass(
        typesStack: Set<ConeKotlinType>
    ): Boolean = context(context.session.typeContext) {
        this.classKind == ClassKind.CLASS
                && this.isInstantiable()
                && this.constructors(context.session).any { it.isPossibleToStub(typesStack) }
    }

    private fun ConeKotlinType.isAnyOf(classIds: Set<ClassId>): Boolean {
        if (this !is ConeClassLikeType) return false
        return lookupTag.classId in classIds
    }

    private fun FirRegularClassSymbol.isInstantiable() = modality in instantiableModalities

    private fun FirRegularClassSymbol.isOverridable() = modality in overridableModalities

    private fun FirConstructorSymbol.isAccessible() = visibility in accessibleVisibility

    object Diagnostics : KtDiagnosticsContainer() {

        override fun getRendererFactory() = MocksCreationDiagnosticRendererFactory()

        val INDIRECT_INTERCEPTION by error2<KtElement, Name, ConeKotlinType>()
        val SEALED_TYPE_CANNOT_BE_INTERCEPTED by error2<KtElement, Name, ConeKotlinType>()
        val FINAL_TYPE_CANNOT_BE_INTERCEPTED by error2<KtElement, Name, ConeKotlinType>()
        val PRIMITIVE_TYPE_CANNOT_BE_INTERCEPTED by error2<KtElement, Name, ConeKotlinType>()
        val FINAL_MEMBERS_TYPE_CANNOT_BE_INTERCEPTED by error3<KtElement, Name, ConeKotlinType, List<FirBasedSymbol<*>>>()
        val NO_PUBLIC_CONSTRUCTOR_TYPE_CANNOT_BE_INTERCEPTED by error2<KtElement, Name, ConeKotlinType>()
        val NO_CONSTRUCTOR_TO_STUB by error2<KtElement, Name, List<Pair<ConeKotlinType, StubError>>>()
        val MULTIPLE_SUPER_CLASSES_FOR_MOCK_MANY by error2<KtElement, Name, List<ConeKotlinType>>()
        val DUPLICATE_TYPES_FOR_MOCK_MANY by error3<KtElement, ConeKotlinType, Name, String>()
        val FUNCTIONAL_TYPE_ON_JS_FOR_MOCK_MANY by error2<KtElement, ConeKotlinType, Name>()
    }

    companion object {

        private val accessibleVisibility = setOf(Visibilities.Public, Visibilities.Internal)
        private val overridableModalities = setOf(Modality.ABSTRACT, Modality.OPEN)
        private val instantiableModalities = setOf(Modality.OPEN, Modality.FINAL)

        private val defaultTypesToStub = StandardClassIds.primitiveTypes +
                StandardClassIds.unsignedTypes +
                StandardClassIds.Number +
                StandardClassIds.KClass +
                StandardClassIds.Unit +
                StandardClassIds.String +
                StandardClassIds.primitiveArrayTypeByElementType.values.toSet() +
                StandardClassIds.unsignedArrayTypeByElementType.values.toSet() +
                StandardClassIds.Array
    }
}

enum class StubError {

    Recursion, ClassInstantiationNeedsOptIn, ClassOverrideNeedsOptIn, NoAccessibleConstructors;

    companion object {

        fun renderer() = Renderer<StubError> {
            when (it) {
                Recursion -> "This class causes an instantiation cycle and its instance cannot be supplied."
                ClassInstantiationNeedsOptIn -> "This class could be instantiated," +
                        " but it requires explicit permission due to potential execution of unintended code." +
                        " Enable it in your Gradle file with `mokkery.stubs.allowConcreteClassInstantiation` flag."
                ClassOverrideNeedsOptIn -> "This class could be inherited to create a stub," +
                        " but it requires explicit permission due to potential execution of unintended code." +
                        " Enable it in your Gradle file with `mokkery.stubs.allowClassInheritance` flag."
                NoAccessibleConstructors -> "This class cannot be stubbed. No accessible constructors."
            }
        }
    }
}
