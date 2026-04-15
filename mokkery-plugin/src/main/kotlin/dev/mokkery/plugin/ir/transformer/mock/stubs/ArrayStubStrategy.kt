package dev.mokkery.plugin.ir.transformer.mock.stubs

import dev.mokkery.plugin.Kotlin
import dev.mokkery.plugin.core.ir.compat.referenceFunctionsCompat
import dev.mokkery.plugin.core.ir.pluginContext
import dev.mokkery.plugin.ir.irCall
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.isArray
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.util.eraseTypeParameters
import org.jetbrains.kotlin.ir.util.isPrimitiveArray
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.util.capitalizeDecapitalize.decapitalizeAsciiOnly

object ArrayStubStrategy : StubStrategy {

    context(scope: StubStrategyScope)
    override fun provide(type: IrType): Stub? {
        val name = when {
            type.isArray() -> "arrayOf"
            type.isPrimitiveArray() -> type.classOrFail.owner.name.asString().decapitalizeAsciiOnly() + "Of"
            else -> return null
        }
        return stub {
            val arrayFunc = pluginContext
                .referenceFunctionsCompat(CallableId(Kotlin.kotlin, Name.identifier(name)))
                .first()
            scope.builder.irCall(arrayFunc) {
                val typeArgs = (type as IrSimpleType).arguments
                typeArgs.forEachIndexed { i, arg ->
                    typeArguments[i] = arg.typeOrNull ?: symbol.owner.parameters[i].type.eraseTypeParameters()
                }
            }
        }
    }
}

