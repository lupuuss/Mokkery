package dev.mokkery.test

import dev.mokkery.answering.Answer
import dev.mokkery.answering.FunctionScope

class ScopeCapturingAnswer : Answer<Any?> {

    var capturedScope: FunctionScope? = null
    private set

    override fun call(scope: FunctionScope): Any? {
        capturedScope = scope
        return null
    }
}
