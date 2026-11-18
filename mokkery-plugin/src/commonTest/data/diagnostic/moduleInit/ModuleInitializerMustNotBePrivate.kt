import dev.mokkery.annotations.MokkeryModuleInit
import dev.mokkery.configurer.MokkeryConfigurer

<!MODULE_INITIALIZER_MUST_NOT_BE_PRIVATE!>@MokkeryModuleInit
private fun foo() = Unit<!>

<!MODULE_INITIALIZER_MUST_NOT_BE_PRIVATE!>@MokkeryModuleInit
private fun bar(configurer: MokkeryConfigurer) = Unit<!>
