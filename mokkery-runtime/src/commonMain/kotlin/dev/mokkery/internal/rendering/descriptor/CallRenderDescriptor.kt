package dev.mokkery.internal.rendering.descriptor

import dev.mokkery.MokkeryCallScope
import dev.mokkery.call
import dev.mokkery.context.CallArgument
import dev.mokkery.context.Function
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.matcher.CallEntry
import dev.mokkery.internal.rendering.function
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.rendering.MokkeryRenderingScope

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

context(scope: MokkeryRenderingScope)
internal fun CallEntry.asCallRenderDescriptor(): CallRenderDescriptor {
    val trace = this
    val data = scope.function(trace.instanceId, trace.functionId)
    return object : CallRenderDescriptor {
        override val receiver: MokkeryInstanceId get() = trace.instanceId
        override val function: FunctionRenderDescriptor get() = FunctionRenderDescriptor.parse(data.name)
        override val arguments: List<ArgumentRenderDescriptor> get() = data.parameters.mapIndexed { index, parameter ->
            ArgumentRenderDescriptor.Value(CallArgument(trace.args[index], parameter))
        }
    }
}

context(scope: MokkeryRenderingScope)
internal fun CallTemplate.asCallRenderDescriptor(): CallRenderDescriptor {
    val template = this
    val data = scope.function(template.instanceId, template.functionId)
    return object : CallRenderDescriptor {
        override val receiver: MokkeryInstanceId get() = template.instanceId
        override val function: FunctionRenderDescriptor get() = FunctionRenderDescriptor.parse(data.name)
        override val arguments: List<ArgumentRenderDescriptor> get() = data.parameters.mapIndexed { index, parameter ->
            ArgumentRenderDescriptor.Matcher(parameter, template.matchers[index])
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
