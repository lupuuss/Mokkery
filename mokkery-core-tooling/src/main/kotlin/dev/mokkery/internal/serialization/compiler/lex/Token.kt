package dev.mokkery.internal.serialization.compiler.lex

internal sealed interface Token {

    sealed interface Operator : Token {
        data object Plus : Operator {
            override fun toString(): String = "<+>"
        }
        data object Minus : Operator {
            override fun toString() = "<->"
        }
        data object Range : Operator {
            override fun toString() = "<..>"
        }
    }

    sealed interface Parenthesis : Token {
        data object Left : Parenthesis {
            override fun toString() = "<(>"
        }
        data object Right : Parenthesis {
            override fun toString() = "<)>"
        }
    }

    data class Name(val value: String) : Token {
        override fun toString() = "<name|$value>"
    }

    data class StringLiteral(val value: String) : Token {
        override fun toString() = "<str|$value>"
    }

    data class IntLiteral(val value: Int) : Token {
        override fun toString() = "<int|$value>"
    }

    data object Separator : Token {
        override fun toString() = "<sep>"
    }
}
