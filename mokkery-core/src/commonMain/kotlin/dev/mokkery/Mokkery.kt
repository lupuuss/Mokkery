package dev.mokkery

import dev.mokkery.answer.DefaultAnswer
import dev.mokkery.answer.MockAnswer
import dev.mokkery.matcher.CallTemplateRegistry
import dev.mokkery.tracking.CallTemplate
import dev.mokkery.tracking.CallTrace
import dev.mokkery.tracking.CallTraceClock
import dev.mokkery.tracking.matches
import kotlin.reflect.KClass

internal interface Mokkery {

    val mockId: String

    val mockedType: KClass<*>

    val unverifiedTraces: List<CallTrace>

    val allTraces: List<CallTrace>

    fun interceptCall(signature: String, returnType: KClass<*>, vararg args: Any?): Any?

    suspend fun interceptSuspendCall(signature: String, returnType: KClass<*>, vararg args: Any?): Any?

    fun mockCall(template: CallTemplate, answer: MockAnswer<*>)

    fun startTemplateRegistering(registry: CallTemplateRegistry)

    fun stopTemplateRegistering()

    fun markVerified(call: CallTrace)
}

internal interface MokkeryScope {
    val mokkery: Mokkery
}

internal abstract class BaseMokkeryScope(private val mockedType: KClass<*>) : MokkeryScope {

    override val mokkery: Mokkery by lazy { MokkeryImpl(toString(), mockedType) }

    override fun toString(): String {
        return "${mockedType.simpleName}#${hashCode().toString(36)}"
    }
}

private class MokkeryImpl(
    override val mockId: String,
    override val mockedType: KClass<*>
) : Mokkery {

    private var templateRegistering = false
    private var templateRegistry: CallTemplateRegistry? = null
    private val mockedCalls = mutableMapOf<CallTemplate, MockAnswer<*>>()
    override val unverifiedTraces = mutableListOf<CallTrace>()
    override val allTraces = mutableListOf<CallTrace>()

    override fun interceptCall(signature: String, returnType: KClass<*>, vararg args: Any?): Any? {
        return internalInterceptCall(signature, returnType, args).call(returnType, *args)
    }

    override suspend fun interceptSuspendCall(signature: String, returnType: KClass<*>, vararg args: Any?): Any? {
        return internalInterceptCall(signature, returnType, args).callSuspend(returnType, *args)
    }

    override fun mockCall(template: CallTemplate, answer: MockAnswer<*>) {
        mockedCalls[template] = answer
    }

    override fun startTemplateRegistering(registry: CallTemplateRegistry) {
        templateRegistering = true
        templateRegistry = registry
    }

    override fun stopTemplateRegistering() {
        templateRegistering = false
        templateRegistry = null
    }

    override fun markVerified(call: CallTrace) {
        unverifiedTraces.remove(call)
    }

    override fun toString(): String = "Mokkery#$mockId"

    private fun internalInterceptCall(signature: String, returnType: KClass<*>, args: Array<out Any?>): MockAnswer<*> {
        if (templateRegistering) {
            templateRegistry?.saveTemplate(this, signature, returnType, args)
            return DefaultAnswer
        }
        val trace = CallTrace(this, signature, args.toList(), CallTraceClock.current.nextStamp())
        unverifiedTraces.add(trace)
        allTraces.add(trace)
        return findAnswer(trace)
    }

    private fun findAnswer(trace: CallTrace): MockAnswer<*> {
        return mockedCalls
            .keys
            .find { trace matches it }
            ?.let { mockedCalls.getValue(it) }
            ?: error("Call of $trace not mocked!")
    }
}
