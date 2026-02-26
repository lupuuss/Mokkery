package dev.mokkery.plugin.ir.transformer

import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.ir.IrMokkeryPluginScope
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.applyTransformChildrenVoid
import dev.mokkery.plugin.ir.transformer.core.CoreTransformer
import dev.mokkery.plugin.ir.transformer.core.log
import dev.mokkery.plugin.ir.transformer.core.referenced
import dev.mokkery.plugin.ir.transformer.mock.replaceMockCall
import dev.mokkery.plugin.ir.transformer.mock.replaceMockManyCall
import dev.mokkery.plugin.ir.transformer.mock.replaceSpyCall
import dev.mokkery.plugin.ir.transformer.suite.overrideMokkerySuiteScopeIfNotOverridden
import dev.mokkery.plugin.ir.transformer.templating.MatchersCompiler
import dev.mokkery.plugin.ir.transformer.templating.replaceEvery
import dev.mokkery.plugin.ir.transformer.templating.replaceEverySuspend
import dev.mokkery.plugin.ir.transformer.templating.replaceVerify
import dev.mokkery.plugin.ir.transformer.templating.replaceVerifySuspend
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.isClass
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.isSubpackageOf
import kotlin.time.TimeSource

class MokkeryRootTransformer(pluginScope: IrMokkeryPluginScope) : CoreTransformer(pluginScope) {

    private val matchersCompiler = MatchersCompiler(this)

    override fun visitClassNew(declaration: IrClass): IrStatement {
        declaration.transformChildrenVoid()
        if (!declaration.isClass) return declaration
        val mokkerySuiteScopeClass = referenced(MokkeryIr.Class.MokkerySuiteScope)
        if (declaration.superTypes.none { it.getClass() == mokkerySuiteScopeClass }) return declaration
        declaration.overrideMokkerySuiteScopeIfNotOverridden()
        return declaration
    }

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        if (declaration is IrSimpleFunction) matchersCompiler.compileIfMatcher(declaration)
        return declaration.applyTransformChildrenVoid()
    }

    override fun visitCall(expression: IrCall): IrExpression {
        val name = expression.symbol.owner.kotlinFqName
        expression.transformChildrenVoid()
        if (!name.isSubpackageOf(Mokkery.dev_mokkery)) return expression
        return when (name) {
            Mokkery.Name.mock -> expression.replaceMockCall()
            Mokkery.Name.mockMany -> expression.replaceMockManyCall()
            Mokkery.Name.spy -> expression.replaceSpyCall()
            Mokkery.Name.every -> expression.replaceEvery(matchersCompiler)
            Mokkery.Name.everySuspend -> expression.replaceEverySuspend(matchersCompiler)
            Mokkery.Name.verify -> expression.replaceVerify(matchersCompiler)
            Mokkery.Name.verifySuspend -> expression.replaceVerifySuspend(matchersCompiler)
            else -> expression
        }
    }

    override fun visitModuleFragment(declaration: IrModuleFragment): IrModuleFragment {
        val time = TimeSource.Monotonic.markNow()
        declaration.transformChildrenVoid()
        log { "Plugin time: ${time.elapsedNow()}" }
        return declaration
    }

}

