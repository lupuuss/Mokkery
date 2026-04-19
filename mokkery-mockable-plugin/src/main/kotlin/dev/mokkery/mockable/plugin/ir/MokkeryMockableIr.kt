package dev.mokkery.mockable.plugin.ir

import dev.mokkery.mockable.plugin.MokkeryMockable
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin

object MokkeryMockableIr {

    val Origin = IrDeclarationOrigin.GeneratedByPlugin(MokkeryMockable.Key)
}
