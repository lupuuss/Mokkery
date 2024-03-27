package dev.mokkery.test

import dev.mokkery.internal.render.Renderer

internal class TestRenderer<T>(var calls: (value: T) -> String) : Renderer<T> {

    private val _recordedCalls = mutableListOf<T>()
    val recordedCalls: List<T> = _recordedCalls

    fun returns(value: String) {
        calls = { _ -> value }
    }

    override fun render(value: T): String {
        _recordedCalls += value
        return calls(value)
    }
}
