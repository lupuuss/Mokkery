package dev.mokkery.plugin.ir

import dev.mokkery.plugin.Mokkery.dev_mokkery
import dev.mokkery.plugin.Mokkery.dev_mokkery_annotations
import dev.mokkery.plugin.Mokkery.dev_mokkery_context
import dev.mokkery.plugin.Mokkery.dev_mokkery_internal
import dev.mokkery.plugin.Mokkery.dev_mokkery_internal_context
import dev.mokkery.plugin.Mokkery.dev_mokkery_internal_defaults
import dev.mokkery.plugin.Mokkery.dev_mokkery_internal_matcher
import dev.mokkery.plugin.Mokkery.dev_mokkery_internal_templating
import dev.mokkery.plugin.Mokkery.dev_mokkery_matcher
import dev.mokkery.plugin.Mokkery.dev_mokkery_templating
import dev.mokkery.plugin.Mokkery.dev_mokkery_verify
import dev.mokkery.plugin.nestedClassId
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

object MokkeryIr {
    object Class {
        val Matcher by dev_mokkery_annotations.refClass
        val MockMode by dev_mokkery.refClass
        val MokkeryScope by dev_mokkery.refClass
        val MokkeryMatcherScope by dev_mokkery_matcher.refClass
        val ArgMatcher by dev_mokkery_matcher.refClass
        val ArgMatcherComposite = dev_mokkery_matcher.refNestedClass("ArgMatcher", "Composite")
        val ArgMatcherEquals = dev_mokkery_matcher.refNestedClass("ArgMatcher", "Equals")
        val CompositeVarArgMatcher by dev_mokkery_internal_matcher.refClass
        val DefaultValuesMatcher by dev_mokkery_internal_matcher.refClass
        val MockMany2 by dev_mokkery.refClass
        val MockMany3 by dev_mokkery.refClass
        val MockMany4 by dev_mokkery.refClass
        val MockMany5 by dev_mokkery.refClass

        val MokkerySuiteScope by dev_mokkery.refClass

        val MokkeryInstanceScope by dev_mokkery.refClass
        val VerifyModeInternals by dev_mokkery_verify.refClass

        val CallArgument by dev_mokkery_context.refClass
        val FunctionParameter = dev_mokkery_context.refNestedClass("Function", "Parameter")
        val SuiteName by dev_mokkery_internal_context.refClass

        val MokkeryTemplatingScope by dev_mokkery_templating.refClass
        val RunTemplateResult by dev_mokkery_internal_templating.refClass

        val DefaultsExtractorFactory by dev_mokkery_internal_defaults.refClass

        fun mockMany(value: Int): IrClassReferencer {
            return mockManyMap[value]
                ?: error("Unsupported types number! Expected value: in ${2..5}; Actual value: $value")
        }

        private val mockManyMap = mapOf(
            2 to MockMany2,
            3 to MockMany3,
            4 to MockMany4,
            5 to MockMany5,
        )
    }

    object Function {
        val internalEvery by dev_mokkery_internal.refFunction
        val internalEverySuspend by dev_mokkery_internal.refFunction
        val internalVerify by dev_mokkery_internal.refFunction
        val internalVerifySuspend by dev_mokkery_internal.refFunction
        val runTemplate by dev_mokkery_internal_templating.refFunction
        val runTemplateSuspend by dev_mokkery_internal_templating.refFunction
        val templatingFunctionParameter by dev_mokkery_internal_templating.refFunction
        val checkMockMemberCallResultAccess by dev_mokkery_internal_templating.refFunction
        val MokkerySuiteScope by dev_mokkery.refFunction
        val createInstanceScope by dev_mokkery_internal.refFunction
        val createInstanceContext by dev_mokkery_internal.refFunction
        val initializeInJsFunctionMock by dev_mokkery_internal.refFunction
        val typeArgumentAt by dev_mokkery_internal.refFunction
        val invokeInstantiationListener by dev_mokkery_internal_context.refFunction
        val createBlockingCallScope by dev_mokkery_internal.refFunction
        val createSuspendCallScope by dev_mokkery_internal.refFunction
        val inlineLiteralsAsMatchers by dev_mokkery_internal_matcher.refFunction
        val throwArguments by dev_mokkery_internal_defaults.refFunction
        val methodWithoutDefaultsError by dev_mokkery_internal_defaults.refFunction
        val matches by dev_mokkery_matcher.refFunction { it.owner.parameters.size == 2 }
        val matchesComposite by dev_mokkery_matcher.refFunction
        val spread by dev_mokkery_internal_matcher.refFunction
        val mokkeryRuntimeError by dev_mokkery_internal.refFunction
    }

    object Property {

        val instanceIdString by dev_mokkery_internal.refProperty
        val spiedObject by dev_mokkery_internal.refProperty
        val callInterceptor by dev_mokkery_internal_context.refProperty
    }

    val Origin = IrDeclarationOrigin.GeneratedByPlugin(Key)

    object Key : GeneratedDeclarationKey() {
        override fun toString(): String = "MokkeryPlugin"
    }
}

val FqName.refClass: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, IrClassReferencer>>
    get() = PropertyDelegateProvider { _: Any?, property ->
        val resolver = IrClassById(ClassId(this, Name.identifier(property.name)))
        ReadOnlyProperty { _, _ -> resolver }
    }

fun FqName.refNestedClass(vararg segments: String): IrClassReferencer {
    return IrClassById(this.nestedClassId(*segments))
}
val FqName.refFunction: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, IrFunctionReferencer>>
    get() = PropertyDelegateProvider { _: Any?, property ->
        val resolver = IrFunctionById(CallableId(this, Name.identifier(property.name)))
        ReadOnlyProperty { _, _ -> resolver }
    }

val FqName.refProperty: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, IrPropertyReferencer>>
    get() = PropertyDelegateProvider { _: Any?, property ->
        val resolver = IrPropertyById(CallableId(this, Name.identifier(property.name)))
        ReadOnlyProperty { _, _ -> resolver }
    }

fun FqName.refFunction(
    predicate: (IrSimpleFunctionSymbol) -> Boolean
): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, IrFunctionReferencer>> {
    return PropertyDelegateProvider { _: Any?, property ->
        val resolver = IrFunctionById(CallableId(this, Name.identifier(property.name)), predicate)
        ReadOnlyProperty { _, _ -> resolver }
    }
}
