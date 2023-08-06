package dev.mokkery.plugin.core

import dev.mokkery.plugin.ir.fqName
import dev.mokkery.plugin.ir.functionId

object Kotlin {

    val kotlin_collections by fqName
    val kotlin by fqName
    object Name {

        val listOf by kotlin_collections.functionId
        val mapOf by kotlin_collections.functionId
    }

    object Function {
        val to by kotlin.function
    }
    object Class {
        val Pair by kotlin.klass
    }
}
