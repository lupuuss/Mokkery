// LANGUAGE: +ContextParameters

import dev.mokkery.verify
import dev.mokkery.templating.MokkeryTemplatingScope

<!TEMPLATING_CANNOT_BE_EXTRACTED_TO_FUNCTIONS!>fun MokkeryTemplatingScope.fooFun() = Unit<!>

<!TEMPLATING_CANNOT_BE_EXTRACTED_TO_FUNCTIONS!>fun barFun(param: MokkeryTemplatingScope) = Unit<!>

<!TEMPLATING_CANNOT_BE_EXTRACTED_TO_FUNCTIONS!>context(ctx: MokkeryTemplatingScope)
fun buzzFun() = Unit<!>

val foo: MokkeryTemplatingScope.() -> Unit = <!TEMPLATING_CANNOT_BE_EXTRACTED_TO_FUNCTIONS!>{ }<!>

val bar: (MokkeryTemplatingScope) -> Unit = <!TEMPLATING_CANNOT_BE_EXTRACTED_TO_FUNCTIONS!>{ }<!>

val buzz: context(MokkeryTemplatingScope) () -> Unit = <!TEMPLATING_CANNOT_BE_EXTRACTED_TO_FUNCTIONS!>{ }<!>
