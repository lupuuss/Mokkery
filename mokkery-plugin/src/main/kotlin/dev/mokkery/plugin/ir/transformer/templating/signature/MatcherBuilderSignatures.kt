package dev.mokkery.plugin.ir.transformer.templating.signature

import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.util.resolveFakeOverrideOrSelf

context(scope: TransformerScope)
val matcherBuilderSignatures: MatcherBuilderSignatures
    get() = scope.mokkeryContext.require(MatcherBuilderSignatures)

class MatcherBuilderSignatures : MokkeryContext.Element {

    private val functions = linkedSetOf<IrFunction>()

    override val key: MokkeryContext.Key<*> get() = Key

    /**
     * Schedules matcher builder [function] for [retype]. For a fake override, its real declaration is scheduled too,
     * because the backend calls that one, and it might never be registered on its own (e.g. it comes from a library).
     */
    fun register(function: IrFunction) {
        functions.add(function)
        functions.add(function.resolveFakeOverrideOrSelf())
    }

    /**
     * Replaces the source signatures of all registered builders with their [matcherCompiledSignature].
     * Must run once, after the whole module is compiled.
     */
    context(scope: TransformerScope)
    fun retype() {
        functions.forEach { it.retypeTo(it.matcherCompiledSignature) }
        functions.clear()
    }

    companion object Key : MokkeryContext.Key<MatcherBuilderSignatures>
}

