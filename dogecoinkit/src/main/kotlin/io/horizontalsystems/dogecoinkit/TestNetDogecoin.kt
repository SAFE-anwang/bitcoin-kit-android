package io.horizontalsystems.dogecoinkit

import io.horizontalsystems.bitcoincore.network.Network

class TestNetDogecoin : Network() {
    override val protocolVersion: Int = 70015
    override var port: Int = 44556

    override var magic: Long = 0xfcc1b7dc
    override var bip32HeaderPub: Int = 0X043587CF
    override var bip32HeaderPriv: Int = 0x04358394
    override var addressVersion: Int = 111
    override var addressSegwitHrp: String = "tdge"
    override var addressScriptVersion: Int = 0x32
    override var coinType: Int = 1
    override val blockchairChainId: String = "dogecoin"

    override val maxBlockSize = 1_000_000
    override val dustRelayTxFee = 3000 // https://github.com/bitcoin/bitcoin/blob/c536dfbcb00fb15963bf5d507b7017c241718bf6/src/policy/policy.h#L50

    override val syncableFromApi = false

    override var dnsSeeds = listOf(
            "testseed.jrn.me.uk"
    )
}
