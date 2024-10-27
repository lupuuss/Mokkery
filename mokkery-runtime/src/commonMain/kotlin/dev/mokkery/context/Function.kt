package dev.mokkery.context

import dev.drewhamilton.poko.Poko
import kotlin.reflect.KClass

/**
 * Represents a Kotlin function.
 */
@Poko
public class Function internal constructor(
    public val name: String,
    public val parameters: List<Parameter>,
    public val returnType: KClass<*>
) {

    /**
     * Represents a Kotlin function parameter.
     */
    @Poko
    public class Parameter internal constructor(
        public val name: String,
        public val type: KClass<*>,
        public val isVararg: Boolean
    )
}
