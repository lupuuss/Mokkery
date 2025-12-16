package dev.mokkery.plugin.stubs

import dev.mokkery.plugin.ir.irGetEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.findDeclaration
import org.jetbrains.kotlin.ir.util.isEnumClass

object EnumStubStrategy : StubStrategy {

    context(scope: StubStrategyScope)
    override fun provide(type: IrType): Stub? {
        val cls = type.classOrNull?.owner ?: return null
        if (!cls.isEnumClass) return null
        return cls.findDeclaration<IrEnumEntry> { true }?.let {
            stub(scope.builder.irGetEnumEntry(cls, it.name.asString()))
        }
    }
}
