package dev.mokkery.plugin

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object Mokkery {

    object Package {
        val mokkery = FqName("dev.mokkery")
        val mokkery_internal = FqName("dev.mokkery.internal")
        val mokkery_internal_templating = FqName("dev.mokkery.internal.templating")
        val mokkery_internal_tracing = FqName("dev.mokkery.internal.tracing")
    }

    object Function {
        val mock = Package.mokkery.child(Name.identifier("mock"))
        val spy = Package.mokkery.child(Name.identifier("spy"))
        val every = Package.mokkery.child(Name.identifier("every"))
        val everySuspend = Package.mokkery.child(Name.identifier("everySuspend"))
        val verify = Package.mokkery.child(Name.identifier("verify"))
        val verifySuspend = Package.mokkery.child(Name.identifier("verifySuspend"))
    }

    object ClassId {
        val MokkeryInterceptor = ClassId(Package.mokkery_internal,Name.identifier("MokkeryInterceptor"))
        val MokkeryInterceptorScope = ClassId(Package.mokkery_internal,Name.identifier("MokkeryInterceptorScope"))

        val MokkeryMock = ClassId(Package.mokkery_internal, Name.identifier("MokkeryMock"))
        val MokkeryMockScope = ClassId(Package.mokkery_internal, Name.identifier("MokkeryMockScope"))

        val MokkerySpy = ClassId(Package.mokkery_internal, Name.identifier("MokkerySpy"))
        val MokkerySpyScope = ClassId(Package.mokkery_internal, Name.identifier("MokkerySpyScope"))
        val CallArg = ClassId(Package.mokkery_internal_tracing, Name.identifier("CallArg"))

        val TemplatingInterceptor = ClassId(
            Package.mokkery_internal_templating,
            Name.identifier("TemplatingInterceptor")
        )

        val MockMode = ClassId(Package.mokkery,Name.identifier("MockMode"))
    }
    object FunctionId {
        val MokkeryMock = CallableId(Package.mokkery_internal,Name.identifier("MokkeryMock"))
        val MokkerySpy = CallableId(Package.mokkery_internal,Name.identifier("MokkerySpy"))

        val internalEvery = CallableId(Package.mokkery_internal,Name.identifier("internalEvery"))
        val internalEverySuspend = CallableId(Package.mokkery_internal,Name.identifier("internalEverySuspend"))
        val internalVerify = CallableId(Package.mokkery_internal,Name.identifier("internalVerify"))
        val internalVerifySuspend = CallableId(Package.mokkery_internal,Name.identifier("internalVerifySuspend"))
    }
}
