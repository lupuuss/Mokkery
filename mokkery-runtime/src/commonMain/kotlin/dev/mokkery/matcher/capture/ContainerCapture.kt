package dev.mokkery.matcher.capture

/**
 * A capture that is able to store elements.
 */
public interface ContainerCapture<T> : Capture<T> {

    /**
     * Returns stored elements
     */
    public val values: List<T>
}
