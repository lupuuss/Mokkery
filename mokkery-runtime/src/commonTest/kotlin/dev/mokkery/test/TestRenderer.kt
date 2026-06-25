package dev.mokkery.test

import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.rendering.Renderer
import dev.mokkery.rendering.MokkeryRenderingScope

internal class TestRenderer<T>(
    override val key: MokkeryContext.Key<*> = Renderer.Key<T>("TEST_RENDERER"),
    var calls: context(MokkeryRenderingScope)(value: T) -> String
) : Renderer<T> {

    private val _recordedCalls = mutableListOf<T>()
    val recordedCalls: List<T> = _recordedCalls

    fun returns(value: String) {
        calls = { _ -> value }
    }

    context(scope: MokkeryRenderingScope)
    override fun render(value: T): String {
        _recordedCalls += value
        return calls(value)
    }
}
