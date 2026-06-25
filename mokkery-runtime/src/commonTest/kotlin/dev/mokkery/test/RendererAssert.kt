package dev.mokkery.test

import dev.mokkery.internal.rendering.Renderer
import dev.mokkery.rendering.MokkeryRenderingScope
import kotlin.test.assertEquals

context(scope: MokkeryRenderingScope)
internal fun <T> Renderer<T>.assert(input: T, expected: () -> String) {
    assertEquals(expected(), render(input))
}
