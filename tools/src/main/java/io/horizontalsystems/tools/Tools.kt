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
            previousBlockHeaderHash = HashUtils.toBytesAsLE("ffb185f7e0ba5c72ae9cc45178575eb7029db5e8cca7f747b532cd0b9ab74a2c"),
            merkleRoot = HashUtils.toBytesAsLE("17fac79b2c00f3e13740d3e04c0e5f1f028cef65127ba7331479b069a638163c"),
            timestamp = 1694780205,
            bits = 0,
            nonce = 123806790,
            hash = HashUtils.toBytesAsLE("3ef061097a1edea33f712d1f99e4be8567e1094a5a01e79f4d196eaad5cd6ab0")
    ), 5178101)

    BuildCheckpoints().build(checkpointBlock)
}
