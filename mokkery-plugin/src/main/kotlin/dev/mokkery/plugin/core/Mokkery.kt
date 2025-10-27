package dev.mokkery.plugin.core

import dev.mokkery.plugin.ir.fqName
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
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
    val dev_mokkery_internal_answering_autofill by fqName
    val dev_mokkery_internal_defaults by fqName

    object Class {

        val Matcher by dev_mokkery_annotations.klass
        val MockMode by dev_mokkery.klass
        val MokkeryScope by dev_mokkery.klass
        val MokkeryMatcherScope by dev_mokkery_matcher.klass
        val ArgMatcher by dev_mokkery_matcher.klass
        val CompositeVarArgMatcher by dev_mokkery_internal_matcher.klass
        val DefaultValueMatcher by dev_mokkery_internal_matcher.klass
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
        val createInstanceScope by dev_mokkery_internal.function
        val createInstanceContext by dev_mokkery_internal.function
        val initializeInJsFunctionMock by dev_mokkery_internal.function
        val typeArgumentAt by dev_mokkery_internal.function
        val autofillConstructor by dev_mokkery_internal_answering_autofill.function
        val invokeInstantiationListener by dev_mokkery_internal_context.function
        val createBlockingCallScope by dev_mokkery_internal.function
        val createSuspendCallScope by dev_mokkery_internal.function
        val inlineLiteralsAsMatchers by dev_mokkery_internal_matcher.function
        val throwArguments by dev_mokkery_internal_defaults.function
        val methodWithoutDefaultsError by dev_mokkery_internal_defaults.function
        val matches by dev_mokkery_matcher.function { it.owner.parameters.size == 2 }
        val matchesComposite by dev_mokkery_matcher.function
        val spread by dev_mokkery_internal_matcher.function
    }

    object Property {

        val instanceIdString by dev_mokkery_internal.property
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
        val TemplatingLambda by dev_mokkery_internal_annotations.classId
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

fun FqName.function(
    predicate: (IrSimpleFunctionSymbol) -> Boolean
): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, FunctionResolver>> {
    return PropertyDelegateProvider { _: Any?, property ->
        val resolver = FunctionById(CallableId(this, Name.identifier(property.name)), predicate)
        ReadOnlyProperty { _, _ -> resolver }
    }
}
