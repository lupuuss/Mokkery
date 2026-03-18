package dev.mokkery.plugin

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
