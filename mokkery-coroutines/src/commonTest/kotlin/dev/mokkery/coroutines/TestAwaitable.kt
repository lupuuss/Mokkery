package dev.mokkery.coroutines

import dev.mokkery.coroutines.answering.Awaitable
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable

class TestAwaitable<T>(
    var await: suspend (MokkerySuspendCallScope) -> T,
    var desc: () -> String
) : Awaitable<T>, Renderable {
    override suspend fun await(scope: MokkerySuspendCallScope): T = await.invoke(scope)

    context(scope: MokkeryRenderingScope)
    override fun render(): String  = desc.invoke()
}
