package dev.mokkery.plugin.ir

import dev.mokkery.plugin.core.Kotlin
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name


fun IrPluginContext.referenceKotlinFunctions(name: String) = referenceFunctions(
    CallableId(Kotlin.kotlin, Name.identifier(name))
)


fun IrPluginContext.referenceKotlinCollectionsFunctions(name: String) = referenceFunctions(
    CallableId(Kotlin.kotlin_collections, Name.identifier(name))
)

fun IrPluginContext.referenceKotlinSequencesFunctions(name: String) = referenceFunctions(
    CallableId(Kotlin.kotlin_sequences, Name.identifier(name))
)
