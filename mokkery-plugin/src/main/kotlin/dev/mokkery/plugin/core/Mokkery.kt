package dev.mokkery.plugin.core

import dev.mokkery.plugin.ext.fqName
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

object Mokkery {

    val dev_mokkery by fqName
    val dev_mokkery_internal by fqName
    val dev_mokkery_internal_templating by fqName
    val dev_mokkery_matcher by fqName
    val dev_mokkery_internal_tracing by fqName
    val dev_mokkery_internal_dynamic by fqName

    object Class {

        val MockMode by dev_mokkery.klass
        val VerifyMode by dev_mokkery.klass
        val SoftVerifyMode by dev_mokkery.klass
        val ArgMatchersScope by dev_mokkery_matcher.klass

        val MokkeryInterceptor by dev_mokkery_internal.klass
        val MokkeryInterceptorScope by dev_mokkery_internal.klass

        val MokkeryMock by dev_mokkery_internal.klass
        val MokkeryMockScope by dev_mokkery_internal.klass

        val MokkerySpy by dev_mokkery_internal.klass
        val MokkerySpyScope by dev_mokkery_internal.klass
        val CallContext by dev_mokkery_internal.klass

        val CallArg by dev_mokkery_internal_tracing.klass

        val TemplatingInterceptor by dev_mokkery_internal_templating.klass
        val TemplatingScope by dev_mokkery_internal_templating.klass
        val MokkeryScopeLookup by dev_mokkery_internal_dynamic.klass
    }

    object Function {
        val MokkeryMock by dev_mokkery_internal.function
        val MokkerySpy by dev_mokkery_internal.function

        val internalEvery by dev_mokkery_internal.function
        val internalEverySuspend by dev_mokkery_internal.function
        val internalVerify by dev_mokkery_internal.function
        val internalVerifySuspend by dev_mokkery_internal.function

        val TemplatingScope by dev_mokkery_internal_templating.function
        val MokkeryMockScope by dev_mokkery_internal.function
        val MokkerySpyScope by dev_mokkery_internal.function
        val generateMockId by dev_mokkery_internal.function
    }

    object Name {
        val mock by dev_mokkery.fqName
        val spy by dev_mokkery.fqName
        val every by dev_mokkery.fqName
        val everySuspend by dev_mokkery.fqName
        val verify by dev_mokkery.fqName
        val verifySuspend by dev_mokkery.fqName
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
