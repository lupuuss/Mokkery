package dev.mokkery.plugin.ext

import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.Name

fun IrFactory.buildClass(name: Name, vararg superTypes: IrType): IrClass {
    val newClass = buildClass { this.name = name }
    newClass.superTypes = superTypes.toList()
    newClass.thisReceiver = newClass.buildThisValueParam()
    return newClass
}
