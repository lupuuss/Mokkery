package dev.mokkery.matcher.capture

import dev.drewhamilton.poko.Poko
import kotlin.reflect.KProperty

/**
 * [Capture] that stores only the last value.
 */
public interface SlotCapture<T> : ContainerCapture<T> {

    /**
     * Contains only latest value.
     */
    override val values: List<T>

    /**
     * If no captured values it returns [Value.Absent].
     * Otherwise, it returns the last captured value wrapped with [Value.Present]
     */
    public val value: Value<T>

    public sealed interface Value<out T> {

        /**
         * Represents last captured value.
         */
        @Poko
        public class Present<out T>(public val value: T): Value<T> {

            override fun toString(): String = "Present(value=$value)"
        }

        /**
         * Represents absence of captured value.
         */
        public data object Absent: Value<Nothing>
    }
}

/**
 * Returns true if [SlotCapture.value] is [SlotCapture.Value.Present].
 */
public val <T> SlotCapture<T>.isPresent: Boolean get() = value is SlotCapture.Value.Present

/**
 * Returns true if [SlotCapture.value] is [SlotCapture.Value.Absent].
 */
public val <T> SlotCapture<T>.isAbsent: Boolean get() = value is SlotCapture.Value.Absent

/**
 * Returns unwrapped [SlotCapture.value] if it is [SlotCapture.Value.Present]. Otherwise, it returns null.
 */
public fun <T> SlotCapture<T>.getIfPresent(): T? = when (val value = value) {
    is SlotCapture.Value.Present -> value.value
    SlotCapture.Value.Absent -> null
}

/**
 * Returns unwrapped [SlotCapture.value] if it is [SlotCapture.Value.Present]. Otherwise, it fails.
 */
public fun <T> SlotCapture<T>.get(): T = when (val value = value) {
    is SlotCapture.Value.Present -> value.value
    SlotCapture.Value.Absent -> error("Expected value in slot, but it is absent!")
}

/**
 * Just like [getIfPresent], but as a [getValue] operator.
 */
public operator fun <T> SlotCapture<T>.getValue(thisRef: Any?, property: KProperty<*>): T? = getIfPresent()

