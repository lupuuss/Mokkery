package dev.mokkery.plugin

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object Kotlin {

    object Package {
        val kotlin_text = FqName("kotlin.text")
    }

    object FunctionId {

        val ToStringRadix = CallableId(Package.kotlin_text, Name.identifier("toString"))

    }
}
