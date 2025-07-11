package com.anwang.safewallet.safekit

import android.content.Context
import android.util.Log
import io.horizontalsystems.bitcoincore.models.Checkpoint
import io.horizontalsystems.bitcoincore.network.Network
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.logging.Logger
import kotlin.collections.ArrayList

class MainNetSafe() : Network() {

    private val logger = Logger.getLogger("MainNetSafe")
    constructor(context: Context) : this() {
        MainSafeNetService(context, this)
    }

    var fallbackBlockDate: String? = null

    override val protocolVersion = 70210
    override val noBloomVersion = 70201

    override var port: Int = 5555

    override var magic: Long = 0xcc6e6962
    override var bip32HeaderPub: Int = 0x0488B21E   // The 4 byte header that serializes in base58 to "xpub".
    override var bip32HeaderPriv: Int = 0x0488ADE4  // The 4 byte header that serializes in base58 to "xprv"
    override var addressVersion: Int = 76
    override var addressSegwitHrp: String = "bc"
    override var addressScriptVersion: Int = 16
    override var coinType: Int = 5
    override var coinTypeAnBao: Int = 1
    override val blockchairChainId: String = "safe-coin"

    override val maxBlockSize = 1_000_000
    override val dustRelayTxFee = 1000 // https://github.com/dashpay/dash/blob/master/src/policy/policy.h#L36

    override var dnsSeeds = listOf(
            "45.118.133.218",
            "172.105.122.90",
            "172.105.122.121",
    )

    val connectFailedIp = ArrayList<String>()

    override fun isMainNode(ip: String?): Boolean {
        if (ip.isNullOrBlank()) {
            return true
        }
        return dnsSeeds.contains(ip)
    }

    override fun getMainNodeIp(list: List<String>): String? {
        if (list.isNullOrEmpty()) {
            return dnsSeeds[Random().nextInt(dnsSeeds.size)]
        }
        val notConnectIp = dnsSeeds.filter { !list.contains(it) && !connectFailedIp.contains(it) }
        if (notConnectIp.isNullOrEmpty()) {
            return null
        }
        return notConnectIp[Random().nextInt(notConnectIp.size)]
    }

    override fun markedFailed(ip: String?) {
        ip?.let {
            logger.info("main node connect fail: $it")
            connectFailedIp.add(it)
        }
    }

    override fun isSafe(): Boolean {
        return true
    }

    override val lastCheckpoint: Checkpoint
        get() = if (fallbackBlockDate == null) {
            super.lastCheckpoint
        } else {
            Checkpoint("${javaClass.simpleName}_${fallbackBlockDate}.checkpoint")
        }
}
