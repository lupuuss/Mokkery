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

@OptIn(ExperimentalTime::class)
class MokkeryIrGenerationExtension(
    private val messageCollector: MessageCollector,
    private val mockMode: MockMode,
    private val verifyMode: VerifyMode
) : IrGenerationExtension {

    private val mockTable = mutableMapOf<IrClass, IrClass>()
    private val spyTable = mutableMapOf<IrClass, IrClass>()

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val time = measureTime {
            moduleFragment.files.forEach { irFile ->
                MockCallsTransformer(
                    pluginContext = pluginContext,
                    messageCollector = messageCollector,
                    irFile = irFile,
                    mockTable = mockTable,
                    mockMode = mockMode,
                ).visitFile(irFile)
                SpyCallsTransformer(
                    pluginContext = pluginContext,
                    messageCollector = messageCollector,
                    irFile = irFile,
                    spyTable = spyTable,
                ).visitFile(irFile)
            }
            val interceptedTypesTable = mockTable + spyTable
            moduleFragment.files.forEach { irFile ->
                CallTrackingTransformer(
                    irFile = irFile,
                    pluginContext = pluginContext,
                    table = interceptedTypesTable,
                    verifyMode = verifyMode,
                ).visitFile(irFile)
            }
        }
        messageCollector.log { "Plugin time: $time" }
    }
}
