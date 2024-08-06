package dev.mokkery.coroutines

import dev.mokkery.answering.FunctionScope
import dev.mokkery.coroutines.answering.Awaitable

class TestAwaitable<T>(
    var await: suspend (FunctionScope) -> T,
    var desc: () -> String
) : Awaitable<T> {
    override suspend fun await(scope: FunctionScope): T = await.invoke(scope)

    override fun description(): String = desc.invoke()
}
