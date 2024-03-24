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
            previousBlockHeaderHash = HashUtils.toBytesAsLE("127cdef09c83e39dde3483df325c2b8c5ac9ca0b8665296310319ab6a3795757"),
            merkleRoot = HashUtils.toBytesAsLE("04b5f0b6600d98866e3d5eea43e129518ed8975f0dc2247f031219032de408fc"),
            timestamp = 1710503340,
            bits = 0,
            nonce = 136823790,
            hash = HashUtils.toBytesAsLE("8323b18856b631087e02038f62507230a0990ffd3a453ee41fc42eb5afb007e9")
    ), 5639934)

    BuildCheckpoints().build(checkpointBlock)
}
