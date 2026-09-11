package dev.mokkery.mockable.plugin

import org.jetbrains.kotlin.GeneratedDeclarationKey

object MokkeryMockable {

    data object Key : GeneratedDeclarationKey() {
        override fun toString(): String = "MokkeryMockablePlugin"
    }
}
