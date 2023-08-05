package dev.mokkery.plugin.transformers

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.name.Name
import java.util.*

fun IrClass.createUniqueMockName(type: String) = name
    .asString()
    .plus(UUID.randomUUID().toString().replace("-", ""))
    .plus(type)
    .let(Name::identifier)
