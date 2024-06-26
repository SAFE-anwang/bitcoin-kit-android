package io.horizontalsystems.bitcoincore.transactions.builder

import io.horizontalsystems.bitcoincore.models.Address
import io.horizontalsystems.bitcoincore.models.Transaction
import io.horizontalsystems.bitcoincore.models.TransactionOutput
import io.horizontalsystems.bitcoincore.storage.FullTransaction
import io.horizontalsystems.bitcoincore.storage.InputToSign
import io.horizontalsystems.bitcoincore.utils.JsonUtils

class MutableTransaction(isOutgoing: Boolean = true) {

    var unlockedHeight : Long? = null

    var reverseHex: String? = null

    var transaction = Transaction(2, 0)
    val inputsToSign = mutableListOf<InputToSign>()
    var outputs = listOf<TransactionOutput>()

    lateinit var recipientAddress: Address
    var recipientValue = 0L
    var memo: String? = null
    var changeAddress: Address? = null
    var changeValue = 0L

    private val pluginData = mutableMapOf<Byte, ByteArray>()

    init {
        transaction.status = Transaction.Status.NEW
        transaction.isMine = true
        transaction.isOutgoing = isOutgoing
    }

    // 计算输出手续费
    fun getPluginDataOutputSize(): Int {
        if (reverseHex != null && !reverseHex!!.startsWith("73616665")) { // 线性锁仓
            val lineLock = JsonUtils.stringToObj(reverseHex!!)
            return if (pluginData.isNotEmpty()) {
                lineLock.outputSize + 1 + pluginData.map { 1 + it.value.size }.sum()
            } else {
                0
            }
        } else { // 其他
            return if (pluginData.isNotEmpty()) {
                1 + pluginData.map { 1 + it.value.size }.sum()
            } else {
                0
            }
        }
    }

    fun addInput(inputToSign: InputToSign) {
        inputsToSign.add(inputToSign)
    }

    fun addPluginData(id: Byte, data: ByteArray) {
        pluginData[id] = data
    }

    fun getPluginData(): Map<Byte, ByteArray> {
        return pluginData
    }

    fun build(): FullTransaction {
        return FullTransaction(transaction, inputsToSign.map { it.input }, outputs)
    }

}
