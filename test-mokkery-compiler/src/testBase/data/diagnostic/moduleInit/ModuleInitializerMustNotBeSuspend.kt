import dev.mokkery.annotations.MokkeryModuleInit
import dev.mokkery.configurer.MokkeryConfigurer

<!MODULE_INITIALIZER_MUST_NOT_BE_SUSPEND!>@MokkeryModuleInit
suspend fun foo() = Unit<!>

<!MODULE_INITIALIZER_MUST_NOT_BE_SUSPEND!>@MokkeryModuleInit
suspend fun bar(configurer: MokkeryConfigurer) = Unit<!>
