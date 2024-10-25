package dev.mokkery.internal.render

import dev.mokkery.internal.utils.description

internal object ValueDescriptionRenderer : Renderer<Any?> {
    override fun render(value: Any?): String = value.description()

}
