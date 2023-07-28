package dev.mokkery.plugin

import dev.mokkery.MockMode
import dev.mokkery.plugin.transformers.CallTrackingTransformer
import dev.mokkery.plugin.transformers.MockCallsTransformer
import dev.mokkery.plugin.transformers.SpyCallsTransformer
import dev.mokkery.verify.VerifyMode
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class MokkeryIrGenerationExtension(
    private val messageCollector: MessageCollector,
    private val mockMode: MockMode,
    private val verifyMode: VerifyMode
) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val time = measureTime {
            moduleFragment.files.forEach { irFile ->
                val mockTable = mutableMapOf<IrClass, IrClass>()
                val spyTable = mutableMapOf<IrClass, IrClass>()
                MockCallsTransformer(
                    pluginContext = pluginContext,
                    messageCollector = messageCollector,
                    irFile = irFile,
                    mockMode = mockMode,
                    mockTable = mockTable,
                ).visitFile(irFile)
                SpyCallsTransformer(
                    pluginContext = pluginContext,
                    messageCollector = messageCollector,
                    irFile = irFile,
                    spyTable = spyTable
                ).visitFile(irFile)
            }
            moduleFragment.files.forEach { irFile ->
                CallTrackingTransformer(
                    irFile = irFile,
                    pluginContext = pluginContext,
                    verifyMode = verifyMode,
                ).visitFile(irFile)
            }
        }
        messageCollector.log { "Plugin time: $time" }
    }
}
