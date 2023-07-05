package dev.mokkery.plugin

import dev.mokkery.plugin.ext.fqName
import dev.mokkery.plugin.ext.function
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object Kotlin {

    val kotlin_text by fqName

    object FunctionId {

        val toString by kotlin_text.function
    }
}
