package io.horizontalsystems.bitcoincore.managers

import io.horizontalsystems.bitcoincore.core.IStorage
import io.horizontalsystems.bitcoincore.core.PluginManager
import io.horizontalsystems.bitcoincore.extensions.toHexString
import io.horizontalsystems.bitcoincore.models.BalanceInfo
import io.horizontalsystems.bitcoincore.models.Transaction
import io.horizontalsystems.bitcoincore.storage.UnspentOutput

class UnspentOutputProvider(
    private val storage: IStorage,
    private val confirmationsThreshold: Int = 6,
    val pluginManager: PluginManager
) : IUnspentOutputProvider {

    override fun getSpendableUtxo(): List<UnspentOutput> {
        val lastBlockHeight = storage.lastBlock()?.height ?: 0
        return allUtxo().filter {
            val unlockedHeight = it.output.unlockedHeight;
            if ( unlockedHeight != null && unlockedHeight > lastBlockHeight){
                return@filter false
            }
            pluginManager.isSpendable(it) && it.transaction.status == Transaction.Status.RELAYED
        }
    }

    private fun getUnspendableTimeLockedUtxo() = allUtxo().filter {
        val lastBlockHeight = storage.lastBlock()?.height ?: 0
        return allUtxo().filter {
            val unlockedHeight = it.output.unlockedHeight;
            if ( unlockedHeight != null && unlockedHeight > lastBlockHeight){
                return@filter true
            }
            !pluginManager.isSpendable(it)
        }
    }

    private fun getUnspendableNotRelayedUtxo() = allUtxo().filter {
        it.transaction.status != Transaction.Status.RELAYED
    }

    fun getBalance(): BalanceInfo {
        val spendable = getSpendableUtxo().sumOf { it.output.value }
        val unspendableTimeLocked = getUnspendableTimeLockedUtxo().sumOf { it.output.value }
        val unspendableNotRelayed = getUnspendableNotRelayedUtxo().sumOf { it.output.value }

        return BalanceInfo(spendable, unspendableTimeLocked, unspendableNotRelayed)
    }

    // Only confirmed spendable outputs
    fun getConfirmedSpendableUtxo(): List<UnspentOutput> {
        val lastBlockHeight = storage.lastBlock()?.height ?: 0

        return getSpendableUtxo().filter {
            val block = it.block ?: return@filter false
            return@filter block.height <= lastBlockHeight - confirmationsThreshold + 1
        }
    }

    private fun allUtxo(): List<UnspentOutput> {
        val unspentOutputs = storage.getUnspentOutputs()

        if (confirmationsThreshold == 0) return unspentOutputs
        val lastBlockHeight = storage.lastBlock()?.height ?: 0
        return unspentOutputs.filter {
            // If a transaction is an outgoing transaction, then it can be used
            // even if it's not included in a block yet
            if (it.transaction.isOutgoing) {
                return@filter true
            }

            // If a transaction is an incoming transaction, then it can be used
            // only if it's included in a block and has enough number of confirmations
            val block = it.block ?: return@filter false

            // - Update for Safe-Asset reserve
            val reserve = it.output.reserve;
            if ( reserve != null ){
                if ( reserve.toHexString() != "73616665"  // 普通交易
                    // coinbase 收益
                    && reserve.toHexString() != "7361666573706f730100c2f824c4364195b71a1fcfa0a28ebae20f3501b21b08ae6d6ae8a3bca98ad9d64136e299eba2400183cd0a479e6350ffaec71bcaf0714a024d14183c1407805d75879ea2bf6b691214c372ae21939b96a695c746a6"
                    // safe备注，也是属于safe交易
                    && !reserve.toHexString().startsWith("736166650100c9dcee22bb18bd289bca86e2c8bbb6487089adc9a13d875e538dd35c70a6bea42c0100000a02010012")){
                    return@filter false
                }
            }
            /////////////////////////////////

            if (block.height <= lastBlockHeight - confirmationsThreshold + 1) {
                return@filter true
            }
            false
        }
    }
}
