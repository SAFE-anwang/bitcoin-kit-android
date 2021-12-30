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

        if ( transaction.unlockedHeight != null ){
            transaction.transaction.version = 103;
        }

        if ( transaction.recipientAddressList.isNotEmpty() ){
            val size = transaction.recipientAddressList.size;
            val recipientVal = transaction.recipientValue / size;
            transaction.recipientAddressList.forEachIndexed{ index, address ->
                list.add(TransactionOutput(recipientVal, index, address.lockingScript, address.scriptType, address.string, address.hash,
                    null , transaction.unlockedHeight!!.plus( index * 86400 ), "73616665".hexToByteArray() ) )
            }
        }else{
            transaction.recipientAddress.let {
                if (transaction.unlockedHeight != null){
                    list.add(TransactionOutput(transaction.recipientValue, 0, it.lockingScript, it.scriptType, it.string, it.hash,
                        null , transaction.unlockedHeight , "73616665".hexToByteArray() ) )
                }else{
                    list.add(TransactionOutput(transaction.recipientValue, 0, it.lockingScript, it.scriptType, it.string, it.hash))
                }
            }
        }
        transaction.changeAddress?.let {
            if (transaction.unlockedHeight != null){
                list.add(TransactionOutput(transaction.changeValue, 0, it.lockingScript, it.scriptType, it.string, it.hash,
                    null , 0 , "73616665".hexToByteArray() ) )
            }else{
                list.add(TransactionOutput(transaction.changeValue, 0, it.lockingScript, it.scriptType, it.string, it.hash))
            }
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

        transaction.outputs = sorted
    }

}
