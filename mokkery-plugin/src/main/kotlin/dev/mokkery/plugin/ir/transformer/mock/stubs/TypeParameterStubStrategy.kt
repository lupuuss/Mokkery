package dev.mokkery.plugin.ir.transformer.mock.stubs

import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.eraseTypeParameters
import org.jetbrains.kotlin.ir.util.isTypeParameter

object TypeParameterStubStrategy : StubStrategy {
    context(scope: StubStrategyScope)
    override fun provide(type: IrType): Stub? {
        if (!type.isTypeParameter()) return null
        return strategy.provide(type.eraseTypeParameters())
    }
}
