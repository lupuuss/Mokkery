// LANGUAGE: +ContextParameters

import dev.mokkery.annotations.MokkeryModuleInit
import dev.mokkery.MokkeryScope
import dev.mokkery.configurer.MokkeryConfigurer

@MokkeryModuleInit
fun noParameters() = Unit

@MokkeryModuleInit
internal fun internalNoParameters() = Unit

@MokkeryModuleInit
fun nonUnitReturn(): Int = 1

@MokkeryModuleInit
fun parameter(configurer: MokkeryConfigurer) = Unit

@MokkeryModuleInit
fun MokkeryConfigurer.receiver() = Unit

@MokkeryModuleInit
context(configurer: MokkeryConfigurer)
fun contextParameter() = Unit

typealias Configurer = MokkeryConfigurer

@MokkeryModuleInit
fun typeAliasParameter(configurer: Configurer) = Unit

@MokkeryModuleInit
fun scopeParameter(scope: MokkeryScope) = Unit

@MokkeryModuleInit
fun MokkeryScope.scopeReceiver() = Unit

@MokkeryModuleInit
context(scope: MokkeryScope)
fun scopeContextParameter() = Unit
