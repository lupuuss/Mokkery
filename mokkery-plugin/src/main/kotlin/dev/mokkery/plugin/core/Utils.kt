package dev.mokkery.plugin.core

import kotlin.random.Random
import kotlin.random.nextULong

fun randomString() = Random.nextULong().toString(36)
