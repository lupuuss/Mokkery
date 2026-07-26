package dev.mokkery.configurer

import dev.mokkery.MokkeryScope
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.context.MokkeryContext

/**
 * Temporary, mutable [MokkeryScope] used to configure [MokkeryContext].
 *
 * It is valid only for the time of the configuration block that provides it. Any access to [mokkeryContext]
 * after that block completes fails with [dev.mokkery.MokkeryRuntimeException].
 */
public interface MokkeryConfigurer : MokkeryScope {

    /**
     * [MokkeryContext] of the configured object. Assigning a new value applies it to the configured object.
     */
    @DelicateMokkeryApi
    public override var mokkeryContext: MokkeryContext
}

/**
 * Adds [context] to [mokkeryContext].
 */
@DelicateMokkeryApi
public operator fun MokkeryConfigurer.plusAssign(context: MokkeryContext) {
    mokkeryContext += context
}

/**
 * Removes element with [key] from [mokkeryContext].
 */
@DelicateMokkeryApi
public operator fun MokkeryConfigurer.minusAssign(key: MokkeryContext.Key<*>) {
    mokkeryContext -= key
}
