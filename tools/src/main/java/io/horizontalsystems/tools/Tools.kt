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
            previousBlockHeaderHash = HashUtils.toBytesAsLE("238cf9890ec80e8873f5042977b16cd39119a2ae3093613e996151107cd9c5b1"),
            merkleRoot = HashUtils.toBytesAsLE("f316e930da63e8e5d38782aad3c49aab765e32655b6c064274590b7e06b71813"),
            timestamp = 1745549997,
            bits = 0,
            nonce = 168372090,
            hash = HashUtils.toBytesAsLE("64f37fd0b6ca308fe690244126c30f07e930b461a3a960c3369d5541c2bb9670")
    ), 6663611)

    BuildCheckpoints().build(checkpointBlock)
}
