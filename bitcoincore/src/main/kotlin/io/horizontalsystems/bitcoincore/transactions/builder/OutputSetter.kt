package io.horizontalsystems.bitcoincore.transactions.builder

import io.horizontalsystems.bitcoincore.core.ITransactionDataSorterFactory
import io.horizontalsystems.bitcoincore.extensions.hexToByteArray
import io.horizontalsystems.bitcoincore.models.TransactionDataSortType
import io.horizontalsystems.bitcoincore.models.TransactionOutput
import io.horizontalsystems.bitcoincore.transactions.scripts.OP_RETURN
import io.horizontalsystems.bitcoincore.transactions.scripts.ScriptType

class OutputSetter(private val transactionDataSorterFactory: ITransactionDataSorterFactory) {

    fun setOutputs(transaction: MutableTransaction, sortType: TransactionDataSortType) {
        val list = mutableListOf<TransactionOutput>()

        transaction.recipientAddress.let {
            list.add(TransactionOutput(transaction.recipientValue, 0, it.lockingScript, it.scriptType, it.string, it.hash))
        }

        transaction.changeAddress?.let {
            list.add(TransactionOutput(transaction.changeValue, 0, it.lockingScript, it.scriptType, it.string, it.hash))
        }

        if (transaction.getPluginData().isNotEmpty()) {
            var data = byteArrayOf(OP_RETURN.toByte())
            transaction.getPluginData().forEach {
                data += byteArrayOf(it.key) + it.value
            }

            list.add(TransactionOutput(0, 0, data, ScriptType.NULL_DATA))
        }

        val sorted = transactionDataSorterFactory.sorter(sortType).sortOutputs(list)
        sorted.forEachIndexed { index, transactionOutput ->
            transactionOutput.index = index
        }

        /**
         * UPDATE FOR SAFE - UNLOCKED_HEIGHT TRANSACTION OUTPUT
         */
        val toAddress = transaction.recipientAddress.string
        val unlockedHeight = transaction.unlockedHeight;
        val reverseHex = transaction.reverseHex;
        if (unlockedHeight != null || reverseHex != null) {
            transaction.transaction.version = 103;
            sorted.forEach { transactionOutput ->
                if (transactionOutput.address.equals(toAddress) && unlockedHeight != null) {
                    transactionOutput.unlockedHeight = unlockedHeight
                } else {
                    transactionOutput.unlockedHeight = 0
                }
                if (transactionOutput.address.equals(toAddress) && reverseHex != null) {
                    transactionOutput.reserve = reverseHex.hexToByteArray();
                } else {
                    transactionOutput.reserve = "73616665".hexToByteArray();
                }
            }
        }

        transaction.outputs = sorted
    }

}
