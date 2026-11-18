import dev.mokkery.annotations.MokkeryModuleInit
import dev.mokkery.configurer.MokkeryConfigurer

<!MODULE_INITIALIZER_MUST_NOT_HAVE_TYPE_PARAMETERS!>@MokkeryModuleInit
fun <T> foo() = Unit<!>

<!MODULE_INITIALIZER_MUST_NOT_HAVE_TYPE_PARAMETERS!>@MokkeryModuleInit
fun <T : MokkeryConfigurer> bar(configurer: T) = Unit<!>
