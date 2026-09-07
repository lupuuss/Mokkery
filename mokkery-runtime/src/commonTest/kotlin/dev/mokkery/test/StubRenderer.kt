package dev.mokkery.test

import dev.mokkery.context.MokkeryContext
import dev.mokkery.rendering.Renderer
import dev.mokkery.rendering.MokkeryRenderingScope

internal open class StubRenderer(
    private val name: String = "STUB",
    private val mode: Mode = Mode.NoBreak,
    override val key: MokkeryContext.Key<*> = Renderer.Key<Any?>("RENDERER_$name"),
) : Renderer<Any?> {

    context(scope: MokkeryRenderingScope)
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
        data class RepeatWithBreak(val n: Int) : Mode
    }

    companion object : StubRenderer()
}
