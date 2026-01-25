package dev.mokkery

import dev.mokkery.annotations.InternalMokkeryApi

/**
 * Internal Mokkery config constants.
 */
@InternalMokkeryApi
public object MokkeryConfig {

    public const val GROUP: String = BuildConfig.GROUP
    public const val VERSION: String = BuildConfig.VERSION
    public const val RUNTIME_DEPENDENCY: String = "$GROUP:${BuildConfig.RUNTIME}:$VERSION"
    public const val PLUGIN_ID: String = BuildConfig.PLUGIN_ID
    public const val PLUGIN_ARTIFACT_ID: String = BuildConfig.PLUGIN_ARTIFACT_ID
    public const val MINIMUM_KOTLIN_VERSION: String = BuildConfig.MINIMUM_KOTLIN_VERSION
    public const val COMPILED_KOTLIN_VERSION: String = BuildConfig.COMPILED_KOTLIN_VERSION
}
