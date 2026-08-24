package dev.mokkery.plugin.ir

import dev.mokkery.plugin.core.ir.IrMokkeryPluginScope
import dev.mokkery.plugin.core.cacheKey
import dev.mokkery.plugin.core.caches
import dev.mokkery.plugin.core.getOrPut
import dev.mokkery.plugin.fnv1a64
import dev.mokkery.plugin.ir.compat.irMangleComputerCompat
import org.jetbrains.kotlin.backend.common.serialization.mangle.MangleMode
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

context(scope: IrMokkeryPluginScope)
fun IrDeclaration.computeSignature(): String = caches[signaturesCache].getOrPut(this) {
    irMangleComputerCompat(StringBuilder(256), MangleMode.SIGNATURE).computeMangle(this)
}

context(scope: IrMokkeryPluginScope)
val IrSimpleFunction.mokkeryFunctionId: Long
    get() = mokkeryFunctionIds.first()

context(scope: IrMokkeryPluginScope)
val IrSimpleFunction.mokkeryFunctionIds: List<Long>
    get() = rootSignatures()
        .map { fnv1a64(it) }
        .distinct()
        .sorted()

private val signaturesCache by cacheKey<IrDeclaration, String>()

context(scope: IrMokkeryPluginScope)
fun IrSimpleFunction.rootSignatures(): List<String> {
    val roots = linkedSetOf<IrSimpleFunction>()
    collectRootsInto(roots)
    return roots.map { it.computeSignature() }.distinct().sorted()
}

private fun IrSimpleFunction.collectRootsInto(roots: MutableSet<IrSimpleFunction>) {
    val overridden = overriddenSymbols
    if (overridden.isEmpty()) {
        roots += this
        return
    }
    overridden.forEach { it.owner.collectRootsInto(roots) }
}
