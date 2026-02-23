package dev.mokkery.plugin.ir

import dev.mokkery.plugin.Kotlin.kotlin
import dev.mokkery.plugin.Kotlin.kotlin_collections
import dev.mokkery.plugin.Kotlin.kotlin_sequences
import org.jetbrains.kotlin.ir.util.isVararg

object KotlinIr {
    object Function {
        val to by kotlin.refFunction
        val listOf by kotlin_collections.refFunction { it.owner.parameters.firstOrNull()?.isVararg == true }
        val mapOf by kotlin_collections.refFunction { it.owner.parameters.firstOrNull()?.isVararg == true }
        val emptyList by kotlin_collections.refFunction
        val emptySet by kotlin_collections.refFunction
        val emptyMap by kotlin_collections.refFunction
        val emptySequence by kotlin_sequences.refFunction
        val mutableListOf by kotlin_collections.refFunction { it.owner.parameters.isEmpty() }
        val mutableSetOf by kotlin_collections.refFunction { it.owner.parameters.isEmpty() }
        val mutableMapOf by kotlin_collections.refFunction { it.owner.parameters.isEmpty() }
    }
    object Class {
        val Pair by kotlin.refClass
        val List by kotlin_collections.refClass
    }
}
