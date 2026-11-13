package io.horizontalsystems.bitcoincore.core

import io.horizontalsystems.bitcoincore.blocks.IBlockchainDataListener
import io.horizontalsystems.bitcoincore.extensions.hexToByteArray
import io.horizontalsystems.bitcoincore.extensions.toHexString
import io.horizontalsystems.bitcoincore.extensions.toReversedHex
import io.horizontalsystems.bitcoincore.managers.UnspentOutputProvider
import io.horizontalsystems.bitcoincore.models.BalanceInfo
import io.horizontalsystems.bitcoincore.models.Block
import io.horizontalsystems.bitcoincore.models.BlockInfo
import io.horizontalsystems.bitcoincore.models.Transaction
import io.horizontalsystems.bitcoincore.models.TransactionFilterType
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.models.*
import io.horizontalsystems.bitcoincore.storage.FullTransaction
import io.horizontalsystems.bitcoincore.storage.FullTransactionInfo
import io.horizontalsystems.bitcoincore.storage.TransactionWithBlock
import io.horizontalsystems.bitcoincore.storage.UnspentOutput
import io.horizontalsystems.bitcoincore.storage.UtxoFilters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

@OptIn(FlowPreview::class)
class DataProvider(
        private val storage: IStorage,
        private val unspentOutputProvider: UnspentOutputProvider,
        private val transactionInfoConverter: ITransactionInfoConverter
) : IBlockchainDataListener {

    interface Listener {
        fun onTransactionsUpdate(inserted: List<TransactionInfo>, updated: List<TransactionInfo>)
        fun onTransactionsDelete(hashes: List<String>)
        fun onBalanceUpdate(balance: BalanceInfo)
        fun onLastBlockInfoUpdate(blockInfo: BlockInfo)
    }

    var listener: Listener? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val balanceUpdateFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    //  Getters
    var balance: BalanceInfo = unspentOutputProvider.getBalance()
        private set(value) {
            if (value != field) {
                field = value
                listener?.onBalanceUpdate(field)
            }
        }

    var lastBlockInfo: BlockInfo?
        private set

    init {
        lastBlockInfo = storage.lastBlock()?.let {
            blockInfo(it)
        }

        balanceUpdateFlow
                .debounce(500)
                .onEach { balance = unspentOutputProvider.getBalance() }
                .launchIn(scope)
    }

    override fun onBlockInsert(block: Block) {
        if (block.height > lastBlockInfo?.height ?: 0) {
            val blockInfo = blockInfo(block)

            lastBlockInfo = blockInfo
            listener?.onLastBlockInfoUpdate(blockInfo)
            balanceUpdateFlow.tryEmit(Unit)
        }
    }

    override fun onTransactionsUpdate(inserted: List<Transaction>, updated: List<Transaction>, block: Block?) {
        listener?.onTransactionsUpdate(
                storage.getFullTransactionInfo(inserted.map { TransactionWithBlock(it, block) }).map { transactionInfoConverter.transactionInfo(it) },
                storage.getFullTransactionInfo(updated.map { TransactionWithBlock(it, block) }).map { transactionInfoConverter.transactionInfo(it) }
        )

        balanceUpdateFlow.tryEmit(Unit)
    }

    override fun onTransactionsDelete(hashes: List<String>) {
        listener?.onTransactionsDelete(hashes)
        balanceUpdateFlow.tryEmit(Unit)
    }

    fun clear() {
        scope.cancel()
    }

    suspend fun transactions(fromUid: String?, type: TransactionFilterType? = null, limit: Int? = null): List<TransactionInfo> {
        return withContext(Dispatchers.IO) {
            val fromTransaction = fromUid?.let { storage.getValidOrInvalidTransaction(it) }
            val transactions = storage.getFullTransactionInfo(fromTransaction, type, limit)
            transactions.filter {
                hasRightReserveOutput(it);
            }.map { transactionInfoConverter.transactionInfo(it) }
        }
    }

    private fun hasRightReserveOutput( transaction: FullTransactionInfo):Boolean{
        transaction.outputs.forEach {
            if ( it.reserve != null ){
                val reserve = it.reserve!!;
                if ( reserve.toHexString() != "73616665"  // 普通交易
                    // coinbase 收益
                    && reserve.toHexString() != "7361666573706f730100c2f824c4364195b71a1fcfa0a28ebae20f3501b21b08ae6d6ae8a3bca98ad9d64136e299eba2400183cd0a479e6350ffaec71bcaf0714a024d14183c1407805d75879ea2bf6b691214c372ae21939b96a695c746a6"
                    // safe备注，也是属于safe交易
                    && !reserve.toHexString().startsWith("736166650100c9dcee22bb18bd289bca86e2c8bbb6487089adc9a13d875e538dd35c70a6bea42c0100000a02010012")){
                    return false;
                }
            }
        }
        return true;
    }

    fun getRawTransaction(transactionHash: String): String? {
        val hashByteArray = transactionHash.hexToByteArray().reversedArray()
        return storage.getFullTransactionInfo(hashByteArray)?.rawTransaction
                ?: storage.getInvalidTransaction(hashByteArray)?.rawTransaction
    }

    fun getTransaction(transactionHash: String): TransactionInfo? {
        return storage.getFullTransactionInfo(transactionHash.hexToByteArray().reversedArray())?.let {
            transactionInfoConverter.transactionInfo(it)
        }
    }

    fun getSpendableUtxo(filters: UtxoFilters): List<UnspentOutput> {
        return unspentOutputProvider.getSpendableUtxo(filters)
    }

    fun getSpendableTimeLockUtxo() = unspentOutputProvider.getUnspendableTimeLockedUtxo()

    fun transactionInfo(fullInfo: FullTransactionInfo): TransactionInfo {
        return transactionInfoConverter.transactionInfo(fullInfo)
    }

    private fun blockInfo(block: Block) = BlockInfo(
            block.headerHash.toReversedHex(),
            block.height,
            block.timestamp)

    fun updateLastBlockInfo() {
        lastBlockInfo = storage.lastBlock()?.let {
            blockInfo(it)
        }
//        updateBalance()
    }

    fun updateBalance() {
        balanceUpdateFlow.tryEmit(Unit)
    }
}
