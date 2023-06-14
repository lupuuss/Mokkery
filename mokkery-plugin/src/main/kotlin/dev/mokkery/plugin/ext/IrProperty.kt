package dev.mokkery.plugin.ext

import org.jetbrains.kotlin.ir.builders.declarations.IrFunctionBuilder
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.name.Name

inline fun IrProperty.addSetter(
    builder: IrFunctionBuilder.() -> Unit = {}
): IrSimpleFunction = IrFunctionBuilder().run {
    name = Name.special("<set-${this@addSetter.name}>")
    builder()
    factory.buildFun(builder).also { setter ->
        this@addSetter.setter = setter
        setter.correspondingPropertySymbol = this@addSetter.symbol
        setter.parent = this@addSetter.parent
    }
}

