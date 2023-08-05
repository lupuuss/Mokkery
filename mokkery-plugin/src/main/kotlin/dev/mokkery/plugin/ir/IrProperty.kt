package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.ir.builders.declarations.IrFunctionBuilder
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.name.Name

inline fun IrProperty.addSetter(
    builder: IrFunctionBuilder.() -> Unit = {}
): IrSimpleFunction {
    val setter = factory.buildFun(builder = {
        name = Name.special("<set-${this@addSetter.name}>")
        builder()
    })
    this@addSetter.setter = setter
    setter.correspondingPropertySymbol = this@addSetter.symbol
    setter.parent = this@addSetter.parent
    return setter
}
