package dev.mokkery.plugin.ir.stubs

import dev.mokkery.plugin.ir.irLambdaOf
import dev.mokkery.plugin.ir.isAnyFunction
import dev.mokkery.plugin.ir.stubFunctionBody
import org.jetbrains.kotlin.ir.types.IrType

object LambdaStubStrategy : StubStrategy {

    context(scope: StubStrategyScope)
    override fun provide(type: IrType): Stub? {
        if (!type.isAnyFunction()) return null
        return stub {
            scope.builder.irLambdaOf(type) {
                stubFunctionBody(it)
            }
        }
    }
}
