package dev.mokkery.plugin.ir.transformer.mock

import dev.mokkery.plugin.Cache
import dev.mokkery.plugin.caches
import dev.mokkery.plugin.context.configuration
import dev.mokkery.plugin.defaultMockMode
import dev.mokkery.plugin.ir.Caches
import dev.mokkery.plugin.ir.IrMokkeryKind.Mock
import dev.mokkery.plugin.ir.IrMokkeryKind.Spy
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.findExtensionParam
import dev.mokkery.plugin.ir.findRegularParameters
import dev.mokkery.plugin.ir.forEachIndexedTypeArgument
import dev.mokkery.plugin.ir.irBuiltIns
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irGetEnumEntry
import dev.mokkery.plugin.ir.isAnyFunction
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.platform
import dev.mokkery.plugin.ir.transformer.core.TransformerScope
import dev.mokkery.plugin.ir.transformer.core.declarationIrBuilder
import dev.mokkery.plugin.ir.transformer.core.irGetMokkeryScopeGlobal
import dev.mokkery.plugin.ir.transformer.core.referenced
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.platform.isJs

context(scope: TransformerScope)
fun IrCall.replaceMockCall(): IrExpression {
    val typeToMock = typeArguments.firstOrNull() ?: return this
    val classToMock = typeToMock.getClass() ?: return this
    if (platform.isJs() && classToMock.defaultType.isAnyFunction()) return buildMockJsFunction(this, Mock)
    val mockedClass = cachedMockImplementationClass(
        typeToMock = classToMock,
        cacheKey = Caches.mockClasses,
        builder = { buildMockClass(Mock, it) }
    )
    return declarationIrBuilder {
        irMockConstructorCall(mockedClass, this@replaceMockCall)
    }
}

context(scope: TransformerScope)
fun IrCall.replaceMockManyCall(): IrExpression {
    val classes = typeArguments.mapNotNullTo(mutableSetOf()) { it?.getClass() }
    if (classes.isEmpty()) return this
    val mockedClass = cachedMockImplementationClass(
        typeToMock = classes,
        cacheKey = Caches.mockManyClasses,
        builder = { buildManyMockClass(it.toList()) }
    )
    return declarationIrBuilder {
        irMockConstructorCall(mockedClass, this@replaceMockManyCall)
    }
}

context(scope: TransformerScope)
fun IrCall.replaceSpyCall(): IrExpression {
    val typeToMock = typeArguments.firstOrNull() ?: return this
    val classToMock = typeToMock.getClass() ?: return this
    if (platform.isJs() && classToMock.defaultType.isAnyFunction()) return buildMockJsFunction(this, Spy)
    val spiedClass = cachedMockImplementationClass(
        typeToMock = classToMock,
        cacheKey = Caches.spyClasses,
        builder = { buildMockClass(Spy, it) }
    )
    return declarationIrBuilder {
        irSpyConstructorCall(spiedClass, this@replaceSpyCall)
    }
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.irMockConstructorCall(
    cls: IrClass,
    originalCall: IrCall
) = irCallConstructor(cls.primaryConstructor!!) {
    val regularParams = originalCall.symbol.owner.findRegularParameters()
    arguments[0] = originalCall.symbol.owner
        .findExtensionParam()
        ?.let(originalCall.arguments::get)
        ?: irGetMokkeryScopeGlobal()
    arguments[1] = originalCall.arguments[regularParams[0]] ?: irGetEnumEntry(
        referenced(MokkeryIr.Class.MockMode),
        configuration.defaultMockMode.toString()
    )
    arguments[2] = originalCall.arguments[regularParams[1]] ?: irNull()
    val anyType = irBuiltIns.anyType
    originalCall.typeArguments
        .filterNotNull()
        .forEachIndexedTypeArgument { index, it ->
            arguments[3 + index] = kClassReference(it ?: anyType)
        }
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.irSpyConstructorCall(
    cls: IrClass,
    originalCall: IrCall,
) = irCallConstructor(cls.primaryConstructor!!) {
    val regularParams = originalCall.symbol.owner.findRegularParameters()
    arguments[0] = originalCall.symbol.owner
        .findExtensionParam()
        ?.let(originalCall.arguments::get)
        ?: irGetMokkeryScopeGlobal()
    arguments[1] = irNull()
    arguments[2] = originalCall.arguments[regularParams[1]] ?: irNull()
    arguments[3] = originalCall.arguments[regularParams[0]]
    val anyType = irBuiltIns.anyType
    originalCall.typeArguments
        .filterNotNull()
        .forEachIndexedTypeArgument { index, it ->
            arguments[4 + index] = kClassReference(it ?: anyType)
        }
}

context(scope: TransformerScope)
private fun <T> cachedMockImplementationClass(
    typeToMock: T,
    cacheKey: Cache.Key<T, IrClass>,
    builder: (T) -> IrClass
): IrClass {
    val cache = caches[cacheKey]
    val cached = cache[typeToMock]
    if (cached != null) return cached
    val newClass = builder(typeToMock)
    cache[typeToMock] = newClass
    return newClass
}
