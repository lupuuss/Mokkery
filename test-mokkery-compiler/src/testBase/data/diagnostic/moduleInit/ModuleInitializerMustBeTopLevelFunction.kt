import dev.mokkery.annotations.MokkeryModuleInit

class Foo {

    <!MODULE_INITIALIZER_MUST_BE_TOP_LEVEL_FUNCTION!>@MokkeryModuleInit
    fun member() = Unit<!>

    companion object {

        <!MODULE_INITIALIZER_MUST_BE_TOP_LEVEL_FUNCTION!>@MokkeryModuleInit
        fun companionMember() = Unit<!>
    }
}

object Bar {

    <!MODULE_INITIALIZER_MUST_BE_TOP_LEVEL_FUNCTION!>@MokkeryModuleInit
    fun objectMember() = Unit<!>
}

fun buzz() {
    <!MODULE_INITIALIZER_MUST_BE_TOP_LEVEL_FUNCTION!>@MokkeryModuleInit
    fun local() = Unit<!>
}
