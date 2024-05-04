package dev.mokkery.plugin.transformers

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.name.Name
import java.util.*

fun IrClass.createUniqueMockName(type: String) = name
    .asString()
    .plus(type)
    .plus(UUID.randomUUID().toString().replace("-", ""))
    .let(Name::identifier)

fun IrType.createUniqueManyMockName() = classFqName!!
    .shortName()
    .asString()
    .plus(UUID.randomUUID().toString().replace("-", ""))
    .let(Name::identifier)
