package dev.mokkery.coroutines

import dev.mokkery.coroutines.answering.Awaitable
import dev.mokkery.interceptor.MokkerySuspendCallScope

class TestAwaitable<T>(
    var await: suspend (MokkerySuspendCallScope) -> T,
    var desc: () -> String
) : Awaitable<T> {
    override suspend fun await(scope: MokkerySuspendCallScope): T = await.invoke(scope)

    override fun description(): String = desc.invoke()
}
