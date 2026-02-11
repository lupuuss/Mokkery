package dev.mokkery.plugin

import kotlin.random.Random
import kotlin.random.nextULong

fun randomString() = Random.nextULong().toString(36)
