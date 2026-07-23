package dev.mokkery.plugin.ir.transformer.scope

import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.replaceDeclarationIrBuilder
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.transformer.core.irGetMokkeryFileScope
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression

context(scope: TransformerScope)
fun IrCall.replaceMokkerySuiteScope(): IrExpression = this.replaceDeclarationIrBuilder {
    irCall(referenced(MokkeryIr.Function.suiteScope)) {
        arguments[0] = irGetMokkeryFileScope()
        arguments[1] = this@replaceMokkerySuiteScope.arguments[0]
    }
}
