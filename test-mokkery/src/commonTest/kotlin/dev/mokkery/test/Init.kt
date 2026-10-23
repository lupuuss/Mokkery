package dev.mokkery.test

import dev.mokkery.MokkeryScope
import dev.mokkery.annotations.MokkeryModuleInit
import dev.mokkery.debug.MokkeryCallLogger
import dev.mokkery.interceptor.callHooks

@MokkeryModuleInit
fun init(scope: MokkeryScope) {
    scope.callHooks.beforeAnswering.register(MokkeryCallLogger())
}
