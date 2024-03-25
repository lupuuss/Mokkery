package dev.mokkery.test

import dev.mokkery.internal.render.Renderer
import kotlin.test.assertEquals

internal fun <T> Renderer<T>.assert(input: T, expected: () -> String) {
    assertEquals(expected(), render(input))
}
