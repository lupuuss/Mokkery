package dev.mokkery.plugin.ir

import dev.mokkery.plugin.Kotlin.kotlin
import dev.mokkery.plugin.Kotlin.kotlin_collections

object KotlinIr {
    object Function {
        val to by kotlin.function
    }
    object Class {
        val Pair by kotlin.klass
        val List by kotlin_collections.klass
    }
}
