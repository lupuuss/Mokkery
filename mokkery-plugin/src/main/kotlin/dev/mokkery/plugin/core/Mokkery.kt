package dev.mokkery.plugin.core

import dev.mokkery.plugin.ir.fqName
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

object Mokkery {

    val dev_mokkery by fqName
    val dev_mokkery_annotations by fqName
    val dev_mokkery_internal_annotations by fqName
    val dev_mokkery_templating by fqName
    val dev_mokkery_context by fqName
    val dev_mokkery_internal by fqName
    val dev_mokkery_internal_context by fqName
    val dev_mokkery_matcher by fqName
    val dev_mokkery_internal_templating by fqName
    val dev_mokkery_internal_matcher by fqName
    val dev_mokkery_matcher_varargs by fqName
    val dev_mokkery_internal_answering_autofill by fqName
    val dev_mokkery_internal_defaults by fqName

    object Class {

        val Matcher by dev_mokkery_annotations.klass
        val VarArgMatcherBuilder by dev_mokkery_annotations.klass
        val MockMode by dev_mokkery.klass
        val MokkeryScope by dev_mokkery.klass
        val MokkeryKind by dev_mokkery_internal.klass
        val MokkeryMatcherScope by dev_mokkery_matcher.klass
        val ArgMatcher by dev_mokkery_matcher.klass
        val CompositeVarArgMatcher by dev_mokkery_internal_matcher.klass
        val DefaultValueMatcher by dev_mokkery_internal_matcher.klass
        val VarArgMatcher by dev_mokkery_matcher_varargs.klass
        val VarargMatcherMarker by dev_mokkery_matcher_varargs.klass
        val MockMany2 by dev_mokkery.klass
        val MockMany3 by dev_mokkery.klass
        val MockMany4 by dev_mokkery.klass
        val MockMany5 by dev_mokkery.klass

        val MokkerySuiteScope by dev_mokkery.klass

        val MokkeryInstanceScope by dev_mokkery_internal.klass

        val CallArgument by dev_mokkery_context.klass
        val SuiteName by dev_mokkery_internal_context.klass

        val MokkeryTemplatingScope by dev_mokkery_templating.klass
        val TemplatingParameter by dev_mokkery_internal_templating.klass
        val TemplateOriginalResult by dev_mokkery_internal_templating.klass
        val GlobalMokkeryScope by dev_mokkery_internal.klass

        val DefaultsExtractorFactory by dev_mokkery_internal_defaults.klass

        fun mockMany(value: Int): ClassResolver {
            return mockManyMap[value] ?: error("Unsupported types number! Expected value: in ${2..5}; Actual value: $value")
        }

        private val mockManyMap = mapOf(
            2 to MockMany2,
            3 to MockMany3,
            4 to MockMany4,
            5 to MockMany5,
        )
    }

    object Function {
        val internalEvery by dev_mokkery_internal.function
        val internalEverySuspend by dev_mokkery_internal.function
        val internalVerify by dev_mokkery_internal.function
        val internalVerifySuspend by dev_mokkery_internal.function
        val runTemplate by dev_mokkery_internal_templating.function
        val runTemplateSuspend by dev_mokkery_internal_templating.function
        val MokkerySuiteScope by dev_mokkery.function
        val MokkeryInstanceScope by dev_mokkery_internal.function
        val createMokkeryInstanceContext by dev_mokkery_internal.function
        val initializeInJsFunctionMock by dev_mokkery_internal.function
        val typeArgumentAt by dev_mokkery_internal.function
        val autofillConstructor by dev_mokkery_internal_answering_autofill.function
        val invokeInstantiationListener by dev_mokkery_internal_context.function
        val createMokkeryBlockingCallScope by dev_mokkery_internal.function
        val createMokkerySuspendCallScope by dev_mokkery_internal.function
        val inlineLiteralsAsMatchers by dev_mokkery_internal_matcher.function
        val throwArguments by dev_mokkery_internal_defaults.function
        val methodWithoutDefaultsError by dev_mokkery_internal_defaults.function
        val matches by dev_mokkery_matcher.function
        val matchesComposite by dev_mokkery_matcher.function
    }

    object Property {

        val mockIdString by dev_mokkery_internal.property
        val spiedObject by dev_mokkery_internal.property
        val callInterceptor by dev_mokkery_internal_context.property
    }

    object Name {
        val mock by dev_mokkery.fqName
        val mockMany by dev_mokkery.fqName
        val spy by dev_mokkery.fqName
        val every by dev_mokkery.fqName
        val everySuspend by dev_mokkery.fqName
        val verify by dev_mokkery.fqName
        val verifySuspend by dev_mokkery.fqName
        val ext by dev_mokkery_templating.fqName
        val ctx by dev_mokkery_templating.fqName
    }

    object Callable {
        val mock by dev_mokkery.callableId
        val mockMany by dev_mokkery.callableId
        val spy by dev_mokkery.callableId
        val verify by dev_mokkery.callableId
        val verifySuspend by dev_mokkery.callableId
        val every by dev_mokkery.callableId
        val everySuspend by dev_mokkery.callableId
        val ext by dev_mokkery_templating.callableId
        val ctx by dev_mokkery_templating.callableId
        val matches by dev_mokkery_matcher.callableId
        val matchesComposite by dev_mokkery_matcher.callableId
    }

    object ClassId {
        val MokkeryMatcherScope by dev_mokkery_matcher.classId
        val MokkeryTemplatingScope by dev_mokkery_templating.classId
        val Matcher by dev_mokkery_annotations.classId
        val ArgMatcher by dev_mokkery_matcher.classId
        val ArgMatcherComposite = ClassId(
            packageFqName = dev_mokkery_matcher,
            relativeClassName = FqName.fromSegments(listOf("ArgMatcher", "Composite")),
            isLocal = false
        )
        val VarArgMatcher by dev_mokkery_matcher_varargs.classId
        val TemplatingLambda by dev_mokkery_internal_annotations.classId
        val VarArgMatcherBuilder by dev_mokkery_annotations.classId
    }

    object MocksCreationErrors {

        fun indirectCall(
            typeArgument: String,
            functionName: String
        ) = "''$typeArgument'' is a type parameter! Specific type expected for a ''$functionName'' call!"

        fun sealedTypeCannotBeIntercepted(
            typeName: String,
            functionName: String
        ) = "Type ''$typeName'' is sealed and cannot be used with ''$functionName''!"

        fun finalTypeCannotBeIntercepted(
            typeName: String,
            functionName: String
        ) = "Type ''$typeName'' is final and cannot be used with ''$functionName''!"

        fun primitiveTypeCannotBeIntercepted(
            typeName: String,
            functionName: String
        ) = "Type ''$typeName'' is primitive and cannot be used with ''$functionName''!"

        fun finalMembersTypeCannotBeIntercepted(
            typeName: String,
            functionName: String,
            nonAbstractMembers: String,
        ) =
            "Type ''$typeName'' has final members and cannot be used with ''$functionName''! Final members: $nonAbstractMembers"

        fun noPublicConstructorTypeCannotBeIntercepted(
            typeName: String,
            functionName: String
        ) = "Type ''$typeName'' has no public constructor and cannot be used with ''$functionName''!"

        fun noDuplicatesForMockMany(
            typeName: String,
            functionName: String,
            occurrences: String,
        ) = "Type ''$typeName'' for ''$functionName'' must occur only once, but it occurs $occurrences times!"

        fun singleSuperClass(
            functionName: String,
            superClasses: String
        ) = "Only one super class is acceptable for ''$functionName'' type! Detected super classes: $superClasses"

        fun functionalTypeNotAllowedOnJs(
            typeName: String,
            functionName: String
        ) =
            "Type ''$typeName'' is a functional type and it is not acceptable as an argument for ''$functionName'' on JS platform!"
    }

    object TemplatingErrors {

        fun notLambdaExpression(
            functionName: String,
            param: String,
        ) = "Argument passed to ''$functionName'' for param ''$param'' must be a lambda expression!"
    }

    object TemplatingDeclarationErrors {

        fun extractingTemplatingFunctionsIsCurrentlyIllegal() = "Extracting templating to separate functions is currently illegal! Templating must be performed directly inside lambda passed to `every`/`verify`!"
    }

    object MatcherUsageErrors {

        private val scopeFunctionsHint = "If you're trying to invoke a method with an extension receiver or context parameters, use the `dev.mokkery.templating.ext` or `dev.mokkery.templating.ctx` functions instead."

        fun illegalNestedTemplating(
            function: String
        ) = "''$function'' calls cannot be nested in templating functions or matcher declarations!"

        fun illegalNestedFunctionsMatchers(
            function: String
        ) = "Matchers cannot be used in functions declared inside templating functions or matcher declarations, but used in ''$function''! $scopeFunctionsHint"

        fun illegalNestedClassMatchers(
            klass: String
        ) = "Matchers cannot be used in classes declared inside templating functions or matcher declarations, but used in ''$klass''"


        fun matcherPassedToNonMatcherParam(param: String) = "''$param'' does not accept matchers, but matcher argument is given! Mark parameter with @Matcher annotation, or use regular values!"

        fun illegalVarargsComposite() = "Given expression must be a vararg matcher or a composite of vararg matchers, because all matchers in a composite must be vararg if any of them is."

        fun varargRequiredInAllBranches() = "When vararg matcher is given in one conditional branch, then all other branches must return vararg matchers!"

        fun illegalTryCatch() = "Returning matchers from try/catch is not supported!"

        fun illegalMatcherInNonMemberFunction(function: String): String {
            return "Matchers can only be passed to member functions, but were passed to ''$function''. $scopeFunctionsHint"
        }

        fun illegalMethodInvocationOnMatcher() = "Invoking methods on matchers is illegal."

        fun illegalOperatorUsageOnMatcher(operator: String) = "Operators cannot be used with matchers, but used with ''$operator''"

        fun illegalMatcherInCondition() = "Matcher cannot be used as condition."

        fun variableDefinedOutOfScope(variable: String) = "''$variable'' is defined outside the templating scope and cannot be assigned using matchers."

        fun variableNotMatcher(variable: String) = "''$variable'' is not initialized with a matcher and therefore cannot be reassigned using one."

        fun incompatibleVariableMatcherType(
            variable: String,
            variableMatcherType: String,
            assignedMatcherType: String
        ) = "''$variable'' is initialized with $variableMatcherType matcher and cannot be reassigned with $assignedMatcherType matcher!"

        fun matcherPassedToMethodInMatcherBuilder() = "Passing matchers to methods is not legal inside matcher builders!"

        fun illegalSpreadForVararg() = "The spread operator on matchers is only allowed for vararg matchers or composite matchers that accept only vararg matchers!"

        fun singleVarargMatcherAllowed() = "Only one vararg matcher is allowed!"

        fun varargMatcherUsedWithoutSpread() = "Vararg matcher can only be used with spread operator!"

        fun varargMatcherUsedWithoutVararg() = "Vararg matcher can only be used with varargs!"

        fun matcherPassedToNonMemberFunction() = "Matchers can be only passed to mock methods!"

        fun varargMatcherBuilderMustReturnVarargMatchersOnly() = "Matchers annotated with @dev.mokkery.annotations.VarArgMatcherBuilder must return vararg matchers in all returns!"

        fun matcherReturningVarargMatcherMustBeAnnotated() = "Matcher that returns vararg matchers must be annotated with @dev.mokkery.annotations.VarArgMatcherBuilder!"

        fun matcherUsedWithFinalMethod(function: String) =  "''$function'' must not be used with matchers, because it's final and cannot be mocked!"

        fun matchesWithCompositeArg() = "`dev.mokkery.matcher.matches` cannot be used with `ArgMatcher.Composite` matchers. To register composite matcher use `dev.mokkery.matcher.matchesComposite`."
    }

    object MatchersDeclarationErrors {

        fun matcherMustBeFinal() = "Matcher must be final!"

        fun matcherMustNotBeExternal() = "Matcher must not be external!"

        fun matcherMustBeRegularFunction() = "Matcher must be a regular function!"

        fun argMatcherTypeCannotBeMarkedMatcher(type: String) = "Parameter of type ''$type'' cannot be marked with @dev.mokkery.annotations.Matcher"
    }

    val Origin = IrDeclarationOrigin.GeneratedByPlugin(Key)

    object Key : GeneratedDeclarationKey() {
        override fun toString(): String = "MokkeryPlugin"
    }
}

val FqName.callableId: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, CallableId>>
    get() = PropertyDelegateProvider { _: Any?, property ->
        ReadOnlyProperty { _, _ ->
            CallableId(this, Name.identifier(property.name))
        }
    }

val FqName.classId: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, ClassId>>
    get() = PropertyDelegateProvider { _: Any?, property ->
        ReadOnlyProperty { _, _ ->
            ClassId(this, Name.identifier(property.name))
        }
    }

val FqName.klass: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, ClassResolver>>
    get() = PropertyDelegateProvider { _: Any?, property ->
        val resolver = ClassById(ClassId(this, Name.identifier(property.name)))
        ReadOnlyProperty { _, _ -> resolver }
    }

val FqName.function: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, FunctionResolver>>
    get() = PropertyDelegateProvider { _: Any?, property ->
        val resolver = FunctionById(CallableId(this, Name.identifier(property.name)))
        ReadOnlyProperty { _, _ -> resolver }
    }

val FqName.property: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, PropertyResolver>>
    get() = PropertyDelegateProvider { _: Any?, property ->
        val resolver = PropertyById(CallableId(this, Name.identifier(property.name)))
        ReadOnlyProperty { _, _ -> resolver }
    }
