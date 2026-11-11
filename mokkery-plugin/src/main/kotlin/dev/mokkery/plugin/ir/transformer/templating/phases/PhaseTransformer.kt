package dev.mokkery.plugin.ir.transformer.templating.phases

import dev.mokkery.plugin.core.ir.IrMokkeryPluginScope
import dev.mokkery.plugin.core.ir.transformer.CoreTransformer
import dev.mokkery.plugin.ir.transformer.templating.matcher.matcherFunctionKind
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFunction

/**
 * Base of the templating and matcher phases. It does not descend into matcher builders nested in
 * the transformed function, because the root transformer runs the matcher phases for each of them separately.
 */
abstract class PhaseTransformer(pluginScope: IrMokkeryPluginScope) : CoreTransformer(pluginScope) {

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        val isNested = allScopes.size > 1
        if (isNested && declaration.matcherFunctionKind() != null) return declaration
        return super.visitFunctionNew(declaration)
    }
}
