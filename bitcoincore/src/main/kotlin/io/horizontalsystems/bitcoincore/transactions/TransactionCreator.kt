package io.horizontalsystems.bitcoincore.transactions

import io.horizontalsystems.bitcoincore.core.IPluginData
import io.horizontalsystems.bitcoincore.managers.BloomFilterManager
import io.horizontalsystems.bitcoincore.models.TransactionDataSortType
import io.horizontalsystems.bitcoincore.storage.FullTransaction
import io.horizontalsystems.bitcoincore.storage.UnspentOutput
import io.horizontalsystems.bitcoincore.transactions.builder.TransactionBuilder

class TransactionCreator(
    private val builder: TransactionBuilder,
    private val processor: PendingTransactionProcessor,
    private val transactionSender: TransactionSender,
    private val bloomFilterManager: BloomFilterManager) {

    @Throws
    fun create(toAddress: String, value: Long, feeRate: Int, senderPay: Boolean, sortType: TransactionDataSortType, pluginData: Map<Byte, IPluginData>,unlockedHeight:Long?): FullTransaction {
        return create {
            builder.buildTransaction(toAddress, value, feeRate, senderPay, sortType, pluginData, unlockedHeight)
        }
    }

    @Throws
    fun create(unspentOutput: UnspentOutput, toAddress: String, feeRate: Int, sortType: TransactionDataSortType,unlockedHeight:Long?): FullTransaction {
        return create {
            builder.buildTransaction(unspentOutput, toAddress, feeRate, sortType,unlockedHeight)
        }
    }

    @Throws
    fun createLockSafe(value: Long, feeRate: Int, senderPay: Boolean, sortType: TransactionDataSortType,unlockedHeight:Long): FullTransaction {
        return create {
            builder.buildLockSafeSelfTransaction(value, feeRate, senderPay, sortType, unlockedHeight);
        }
    }

    private fun create(transactionBuilderFunction: () -> FullTransaction): FullTransaction {
        transactionSender.canSendTransaction()

        val transaction = transactionBuilderFunction.invoke()

        try {
            processor.processCreated(transaction)
        } catch (ex: BloomFilterManager.BloomFilterExpired) {
            bloomFilterManager.regenerateBloomFilter()
        }

        try {
            transactionSender.sendPendingTransactions()
        } catch (e: Exception) {
            // ignore any exception since the tx is inserted to the db
        }

        return transaction
    }

    open class TransactionCreationException(msg: String) : Exception(msg)
    class TransactionAlreadyExists(msg: String) : TransactionCreationException(msg)

}
