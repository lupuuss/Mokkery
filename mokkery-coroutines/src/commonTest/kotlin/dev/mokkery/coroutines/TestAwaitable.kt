package dev.mokkery.coroutines

import dev.mokkery.coroutines.answering.Awaitable
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.rendering.MokkeryRenderingScope

class TestAwaitable<T>(
    var await: suspend (MokkerySuspendCallScope) -> T,
    var desc: () -> String
) : Awaitable<T> {
    override suspend fun await(scope: MokkerySuspendCallScope): T = await.invoke(scope)

    context(scope: MokkeryRenderingScope)
    override fun render(): String  = desc.invoke()
}
