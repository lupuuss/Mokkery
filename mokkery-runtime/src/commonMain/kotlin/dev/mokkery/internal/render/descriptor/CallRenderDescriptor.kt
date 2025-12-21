package dev.mokkery.internal.render

import dev.mokkery.MokkeryCallScope
import dev.mokkery.call
import dev.mokkery.context.CallArgument
import dev.mokkery.context.Function
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.matcher.ArgMatcher

internal interface CallRenderDescriptor {

    val receiver: MokkeryInstanceId
    val function: FunctionRenderDescriptor
    val arguments: List<ArgumentRenderDescriptor>
}

internal interface FunctionRenderDescriptor {
    val name: String

    companion object {

        fun parse(rawName: String): FunctionRenderDescriptor = when {
            rawName.startsWith("<get") -> object : GetterRenderDescriptor {
                override val name = rawName.substringAfter("-").substringBefore(">")
            }
            rawName.startsWith("<set") -> object : SetterRenderDescriptor {
                override val name = rawName.substringAfter("-").substringBefore(">")
            }
            else ->  object : FunctionRenderDescriptor {
                override val name = rawName
            }
        }
    }
}

internal interface GetterRenderDescriptor : FunctionRenderDescriptor

internal interface SetterRenderDescriptor : FunctionRenderDescriptor

internal sealed interface ArgumentRenderDescriptor {

    val parameter: Function.Parameter

    data class Value(val arg: CallArgument) : ArgumentRenderDescriptor {
        override val parameter get() = arg.parameter
    }

    data class Matcher(override val parameter: Function.Parameter, val matcher: ArgMatcher<*>): ArgumentRenderDescriptor
}

internal fun CallTrace.asCallRenderDescriptor(): CallRenderDescriptor {
    val trace = this
    return object : CallRenderDescriptor {
        override val receiver: MokkeryInstanceId get() = trace.instanceId
        override val function: FunctionRenderDescriptor get() = FunctionRenderDescriptor.parse(trace.name)
        override val arguments: List<ArgumentRenderDescriptor> get() = trace.args.map { ArgumentRenderDescriptor.Value(it) }
    }
}

internal fun CallTemplate.asCallRenderDescriptor(): CallRenderDescriptor {
    val template = this
    return object : CallRenderDescriptor {
        override val receiver: MokkeryInstanceId get() = template.instanceId
        override val function: FunctionRenderDescriptor get() = FunctionRenderDescriptor.parse(template.name)
        override val arguments: List<ArgumentRenderDescriptor> get() = template.parameters.map {
            ArgumentRenderDescriptor.Matcher(it, template.matchers.getValue(it.name))
        }
    }
}


internal fun MokkeryCallScope.asCallRenderDescriptor(): CallRenderDescriptor {
    val scope = this
    return object : CallRenderDescriptor {
        override val receiver: MokkeryInstanceId get() = scope.instanceSpec.id
        override val function: FunctionRenderDescriptor get() = FunctionRenderDescriptor.parse(scope.call.function.name)
        override val arguments: List<ArgumentRenderDescriptor> get() = scope.call
            .args
            .map { ArgumentRenderDescriptor.Value(it) }
    }
}
