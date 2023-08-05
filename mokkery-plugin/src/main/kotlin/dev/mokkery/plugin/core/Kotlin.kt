package dev.mokkery.plugin.core

import dev.mokkery.plugin.ir.fqName
import dev.mokkery.plugin.ir.functionId

object Kotlin {

    val kotlin_collections by fqName

    object FunctionId {

        val listOf by kotlin_collections.functionId
    }
}
