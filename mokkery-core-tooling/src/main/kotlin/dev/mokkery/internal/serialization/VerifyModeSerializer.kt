package dev.mokkery.internal.serialization

import dev.mokkery.internal.serialization.compiler.ast.FunctionSymbol
import dev.mokkery.internal.serialization.compiler.ast.Parameter
import dev.mokkery.internal.serialization.compiler.ast.PropertySymbol
import dev.mokkery.internal.serialization.compiler.ast.SymbolTable
import dev.mokkery.internal.serialization.compiler.ast.Type
import dev.mokkery.internal.serialization.compiler.ast.constSymbolTable
import dev.mokkery.internal.serialization.compiler.ast.parseAndEvaluate
import dev.mokkery.internal.serialization.compiler.ast.plus
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verify.VerifyMode.Companion.atLeast
import dev.mokkery.verify.VerifyMode.Companion.atMost
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verify.VerifyMode.Companion.inRange
import dev.mokkery.verify.VerifyModeInternals

internal object VerifyModeSerializer : MokkerySerializer<VerifyMode> {

    override fun serialize(obj: VerifyMode): String = when (obj) {
        VerifyModeInternals.ExhaustiveOrder -> "exhaustiveOrder"
        VerifyModeInternals.Exhaustive -> "exhaustive"
        VerifyModeInternals.Not -> "not"
        VerifyModeInternals.Order -> "order"
        is VerifyModeInternals.Soft -> when {
            obj.atLeast == obj.atMost -> "exactly(${obj.atMost})"
            obj.atLeast == 1 && obj.atMost == Int.MAX_VALUE -> "soft"
            obj.atMost == Int.MAX_VALUE -> "atLeast(${obj.atLeast})"
            obj.atLeast == 1 -> "atMost(${obj.atMost})"
            else -> "inRange(${obj.atLeast}..${obj.atMost})"
        }
    }

    override fun deserialize(
        string: String
    ): VerifyMode = parseAndEvaluate(
        string = string,
        type = VerifyModeType,
        symbolTable = SymbolTable.standard + verifySymbolTable
    ) ?: error("Empty expression is not allowed!")

    private val VerifyModeType = Type.Simple("VerifyMode")

    private val verifySymbolTable = constSymbolTable(
        PropertySymbol(
            name = "not",
            type = VerifyModeType,
            access = VerifyMode::not
        ),
        PropertySymbol(
            name = "exhaustive",
            type = VerifyModeType,
            access = VerifyMode::exhaustive,
        ),
        PropertySymbol(
            name = "exhaustiveOrder",
            type = VerifyModeType,
            access = VerifyMode::exhaustiveOrder
        ),
        PropertySymbol(
            name = "order",
            type = VerifyModeType,
            access = VerifyMode::order,
        ),
        PropertySymbol(
            name = "soft",
            type = VerifyModeType,
            access = VerifyMode::soft,
        ),
        FunctionSymbol(
            name = "exactly",
            type = VerifyModeType,
            parameters = listOf(Parameter("n", Type.Int))
        ) { (n: Int) -> exactly(n) },
        FunctionSymbol(
            name = "atMost",
            type = VerifyModeType,
            parameters = listOf(Parameter("n", Type.Int))
        ) { (n: Int) -> atMost(n) },
        FunctionSymbol(
            name = "atLeast",
            type = VerifyModeType,
            parameters = listOf(Parameter("n", Type.Int)),
            body = { (n: Int) -> atLeast(n) }
        ),
        FunctionSymbol(
            name = "inRange",
            type = VerifyModeType,
            parameters = listOf(Parameter("range", Type.IntRange)),
            body = { (range: IntRange) -> inRange(range) }
        ),
    )
}
