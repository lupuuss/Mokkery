package dev.mokkery.plugin

import dev.mokkery.plugin.ext.fqName
import dev.mokkery.plugin.ext.function
import dev.mokkery.plugin.ext.klass

object Mokkery {

    val dev_mokkery by fqName
    val dev_mokkery_internal by fqName
    val dev_mokkery_internal_templating by fqName
    val dev_mokkery_matcher by fqName
    val dev_mokkery_internal_tracing by fqName
    val dev_mokkery_internal_dynamic by fqName

    object Function {
        val mock by dev_mokkery.fqName
        val spy by dev_mokkery.fqName
        val every by dev_mokkery.fqName
        val everySuspend by dev_mokkery.fqName
        val verify by dev_mokkery.fqName
        val verifySuspend by dev_mokkery.fqName
    }

    object ClassId {

        val MockMode by dev_mokkery.klass
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
    object FunctionId {
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
}

