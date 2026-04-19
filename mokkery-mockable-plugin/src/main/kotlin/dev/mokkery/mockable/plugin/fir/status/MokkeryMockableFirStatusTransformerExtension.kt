package dev.mokkery.mockable.plugin.fir.status

import dev.mokkery.mockable.plugin.fir.isMockableAnnotated
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.copyWithNewDefaults
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclarationStatus
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.utils.isLocal
import org.jetbrains.kotlin.fir.extensions.FirStatusTransformerExtension
import org.jetbrains.kotlin.fir.resolve.getContainingClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol

class MokkeryMockableFirStatusTransformerExtension(session: FirSession) : FirStatusTransformerExtension(session) {

    override fun needTransformStatus(declaration: FirDeclaration): Boolean = when (declaration) {
        is FirRegularClass -> declaration.classKind == ClassKind.CLASS && session.isMockableAnnotated(declaration)
        is FirCallableDeclaration -> {
            val parent = declaration.symbol.getContainingClassSymbol() as? FirRegularClassSymbol ?: return false
            if (parent.isLocal) return false
            parent.classKind == ClassKind.CLASS && session.isMockableAnnotated(parent)
        }
        else -> false
    }

    override fun transformStatus(status: FirDeclarationStatus, declaration: FirDeclaration): FirDeclarationStatus {
        return when (status.modality) {
            null -> status.copyWithNewDefaults(modality = Modality.OPEN, defaultModality = Modality.OPEN)
            else -> status.copyWithNewDefaults(defaultModality = Modality.OPEN)
        }
    }
}
