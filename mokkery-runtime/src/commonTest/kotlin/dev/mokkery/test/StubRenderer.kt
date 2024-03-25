package dev.mokkery.test

import dev.mokkery.internal.render.Renderer

internal open class StubRenderer(
    private val name: String = "STUB",
    private val mode: Mode = Mode.NoBreak,
) : Renderer<Any?> {
    override fun render(value: Any?): String {
        val word = "RENDERER_$name"
        return when (mode) {
            Mode.NoBreak -> word
            is Mode.RepeatWithBreak -> "$word\n".repeat(mode.n)
            Mode.WithBreak -> "$word\n"
        }
    }

    sealed interface Mode {
        data object NoBreak : Mode
        data object WithBreak : Mode
        data class RepeatWithBreak(val n: Int) :Mode
    }

    companion object : StubRenderer()
}
