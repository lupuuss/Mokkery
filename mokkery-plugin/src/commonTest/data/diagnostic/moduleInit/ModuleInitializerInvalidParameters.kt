// LANGUAGE: +ContextParameters

import dev.mokkery.annotations.MokkeryModuleInit
import dev.mokkery.MokkeryScope
import dev.mokkery.MokkerySuiteScope
import dev.mokkery.configurer.MokkeryConfigurer
import dev.mokkery.configurer.MokkeryInstanceConfigurer

<!MODULE_INITIALIZER_INVALID_PARAMETERS!>@MokkeryModuleInit
fun wrongType(value: Int) = Unit<!>

<!MODULE_INITIALIZER_INVALID_PARAMETERS!>@MokkeryModuleInit
fun nullable(configurer: MokkeryConfigurer?) = Unit<!>

<!MODULE_INITIALIZER_INVALID_PARAMETERS!>@MokkeryModuleInit
fun subtype(configurer: MokkeryInstanceConfigurer) = Unit<!>

<!MODULE_INITIALIZER_INVALID_PARAMETERS!>@MokkeryModuleInit
fun vararg(vararg configurer: MokkeryConfigurer) = Unit<!>

<!MODULE_INITIALIZER_INVALID_PARAMETERS!>@MokkeryModuleInit
fun twoParameters(first: MokkeryConfigurer, second: MokkeryConfigurer) = Unit<!>

<!MODULE_INITIALIZER_INVALID_PARAMETERS!>@MokkeryModuleInit
fun MokkeryConfigurer.receiverAndParameter(configurer: MokkeryConfigurer) = Unit<!>

<!MODULE_INITIALIZER_INVALID_PARAMETERS!>@MokkeryModuleInit
context(configurer: MokkeryConfigurer)
fun contextAndParameter(other: MokkeryConfigurer) = Unit<!>

<!MODULE_INITIALIZER_INVALID_PARAMETERS!>@MokkeryModuleInit
fun nullableScope(scope: MokkeryScope?) = Unit<!>

<!MODULE_INITIALIZER_INVALID_PARAMETERS!>@MokkeryModuleInit
fun scopeSubtype(scope: MokkerySuiteScope) = Unit<!>

<!MODULE_INITIALIZER_INVALID_PARAMETERS!>@MokkeryModuleInit
fun scopeAndConfigurer(scope: MokkeryScope, configurer: MokkeryConfigurer) = Unit<!>
