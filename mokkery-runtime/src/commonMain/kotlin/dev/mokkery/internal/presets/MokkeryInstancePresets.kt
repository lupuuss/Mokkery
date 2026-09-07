package dev.mokkery.internal.presets

import dev.mokkery.configurer.MokkeryInstanceConfigurer
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.factory.configurer.MockFactoryConfigurer
import dev.mokkery.factory.configurer.SpyFactoryConfigurer
import dev.mokkery.internal.MType

internal val MockFactoryConfigurer.presets: MokkeryInstancePresets
    get() = mokkeryContext.require(MokkeryInstancePresets)

internal val SpyFactoryConfigurer.presets: MokkeryInstancePresets
    get() = mokkeryContext.require(MokkeryInstancePresets)

internal interface MokkeryInstancePresets : MokkeryContext.Element {

    override val key: Key get() = Key

    val isEmpty: Boolean

    fun add(type: MType, block: MokkeryInstanceConfigurer.Block<Any, *>)

    operator fun get(type: MType): List<MokkeryInstanceConfigurer.Block<Any, *>>

    fun copy(): MokkeryInstancePresets

    companion object Key : MokkeryContext.Key<MokkeryInstancePresets>
}

internal fun MokkeryInstancePresets(): MokkeryInstancePresets = MokkeryInstancePresetsImpl()

private class MokkeryInstancePresetsImpl : MokkeryInstancePresets {

    private val typePresets = mutableMapOf<MType, MutableList<MokkeryInstanceConfigurer.Block<Any, *>>>()
    private val starPresets = mutableListOf<StarPreset>()

    override val isEmpty: Boolean
        get() = typePresets.isEmpty() && starPresets.isEmpty()

    override fun add(type: MType, block: MokkeryInstanceConfigurer.Block<Any, *>) {
        val stars = type.arguments.count { it == null }
        if (stars == 0) {
            typePresets.getOrPut(type) { mutableListOf() }.add(block)
            return
        }
        // kept sorted from the least to the most specific, so the order is resolved once per registration
        starPresets.add(StarPreset(type, stars, block))
        starPresets.sortByDescending { it.stars }
    }

    override fun get(type: MType): List<MokkeryInstanceConfigurer.Block<Any, *>> {
        val exact = typePresets[type].orEmpty()
        if (starPresets.isEmpty()) return exact
        val blocks = ArrayList<MokkeryInstanceConfigurer.Block<Any, *>>(starPresets.size + exact.size)
        starPresets.forEach { if (it.type matches type) blocks.add(it.block) }
        if (blocks.isEmpty()) return exact
        blocks.addAll(exact)
        return blocks
    }

    override fun copy(): MokkeryInstancePresets {
        val copied = MokkeryInstancePresetsImpl()
        typePresets.forEach { (type, blocks) ->
            copied.typePresets[type] = blocks.toMutableList()
        }
        copied.starPresets.addAll(starPresets)
        return copied
    }
}

private class StarPreset(
    val type: MType,
    val stars: Int,
    val block: MokkeryInstanceConfigurer.Block<Any, *>,
)

private infix fun MType.matches(other: MType): Boolean {
    if (type != other.type) return false
    if (arguments.size != other.arguments.size) return false
    return arguments.withIndex().all { (index, argument) ->
        argument == null || argument == other.arguments[index]
    }
}
