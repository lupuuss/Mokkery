package dev.mokkery.plugin.ir.stubs

import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.isNullable

object NullableStubStrategy : StubStrategy {

    context(context: StubStrategyScope)
    override fun provide(type: IrType): Stub? = when {
        type.isNullable() -> stub(context.builder.irNull(type))
        else -> null
    }
}
