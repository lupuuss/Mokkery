package dev.mokkery.internal.render

import dev.mokkery.internal.description

internal object ValueDescriptionRenderer : Renderer<Any?> {
    override fun render(value: Any?): String = value.description()

}
