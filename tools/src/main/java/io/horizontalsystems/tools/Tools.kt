package io.horizontalsystems.tools

import io.horizontalsystems.bitcoincore.models.Block
import io.horizontalsystems.bitcoincore.storage.BlockHeader
import io.horizontalsystems.bitcoincore.utils.HashUtils
import java.util.logging.Level
import java.util.logging.Logger

fun main() {
    Logger.getLogger("").level = Level.SEVERE
    ////生成safe时屏掉
//    syncCheckpoints()
    buildCustomCheckpoint()
}

private fun syncCheckpoints() {
    BuildCheckpoints().sync()
    Thread.sleep(5000)
}

private fun buildCustomCheckpoint() {
    val checkpointBlock = Block(BlockHeader(
            version = 536870912,
            previousBlockHeaderHash = HashUtils.toBytesAsLE("d5b3225de72e1536eb08b13b27ea00c6baf12c7f02e5db3abe59914d2ceb6974"),
            merkleRoot = HashUtils.toBytesAsLE("6bd16c6cd2ff38131de68a5b0104f765af3a6aad0cd1256cf47e87890d091ab2"),
            timestamp = 1743523137,
            bits = 0,
            nonce = 165746520,
            hash = HashUtils.toBytesAsLE("d0d966cb8e8240852b5d72e5c7a849ec6d15557c4fba51cf6477797b162706a8")
    ), 6604023)

    BuildCheckpoints().build(checkpointBlock)
}
