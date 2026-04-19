package dev.mokkery.mockable.internal

import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.internal.BuildConfig

@InternalMokkeryApi
public object MokkeryMockableConfig {

    public const val GROUP: String = BuildConfig.GROUP
    public const val VERSION: String = BuildConfig.VERSION
    public const val PLUGIN_ID: String = BuildConfig.PLUGIN_ID
    public const val PLUGIN_ARTIFACT_ID: String = BuildConfig.PLUGIN_ARTIFACT_ID
    public const val ANNOTATIONS_DEPENDENCY: String = "${GROUP}:${BuildConfig.ANNOTATIONS}:${VERSION}"
}
