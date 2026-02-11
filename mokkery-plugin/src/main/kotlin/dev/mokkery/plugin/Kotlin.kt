package dev.mokkery.plugin

import dev.mokkery.plugin.ir.fqName
import dev.mokkery.plugin.ir.function
import dev.mokkery.plugin.ir.functionId
import dev.mokkery.plugin.ir.klass

object Kotlin {

    val kotlin_collections by fqName
    val kotlin_ranges by fqName
    val kotlin_sequences by fqName
    val kotlin by fqName
    object Name {

        val listOf by kotlin_collections.functionId
        val mapOf by kotlin_collections.functionId
    }
}
