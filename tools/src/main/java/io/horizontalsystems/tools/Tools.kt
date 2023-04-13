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
            previousBlockHeaderHash = HashUtils.toBytesAsLE("7dc73f92cbd8b6db50f3a5835f4d2a471d10d8a739e079c131d2d75e3cd4b8ac"),
            merkleRoot = HashUtils.toBytesAsLE("eef4f68fc95168551843345107eb86c65c2c5176584ca3c159f5dc5ecea735ce"),
            timestamp = 1677600045,
            bits = 0,
            nonce = 107308320,
            hash = HashUtils.toBytesAsLE("3f8c9c667a36699f41b1dd97b3a3cdb6aaf7021c28bff6b3ae313ee5173c7b47")
    ), 4656085)

    BuildCheckpoints().build(checkpointBlock)
}
