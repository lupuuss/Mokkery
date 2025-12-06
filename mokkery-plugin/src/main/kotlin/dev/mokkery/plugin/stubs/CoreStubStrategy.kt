package dev.mokkery.plugin.stubs

import dev.mokkery.plugin.ir.kClassReference
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irUnit
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isCharSequence
import org.jetbrains.kotlin.ir.types.isKClass
import org.jetbrains.kotlin.ir.types.isNumber
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.util.defaultValueForType
import org.jetbrains.kotlin.ir.util.eraseTypeParameters

object CoreStubStrategy : StubStrategy {

    context(scope: StubStrategyScope)
    override fun provide(type: IrType): Stub? = when {
        type.isPrimitiveType() -> {
            stub {
                val builder = scope.builder
                IrConstImpl.defaultValueForType(
                    builder.startOffset,
                    builder.endOffset,
                    type,
                )
            }
        }
        type.isNumber() -> stub(scope.builder.irInt(0))
        type.isCharSequence() || type.isString() -> stub(scope.builder.irString(""))
        type.isKClass() -> stub {
            val type = (type as IrSimpleType).arguments
                .firstOrNull()
                ?.typeOrNull
                ?.eraseTypeParameters()
                ?: scope.irBuiltIns.anyType
            scope.builder.kClassReference(type)
        }
        type.isUnit() -> stub(scope.builder.irUnit())
        else -> null
    }
}
