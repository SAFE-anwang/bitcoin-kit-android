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
            previousBlockHeaderHash = HashUtils.toBytesAsLE("b95c46ed6e5c44909484b9e773b12320401b1bb5ce68a516db2af970b39a73c1"),
            merkleRoot = HashUtils.toBytesAsLE("43d4a5b87cb142df0055e72e27c986f231ea39f25277e865cf34fc0dc27d07fa"),
            timestamp = 1733076765,
            bits = 0,
            nonce = 156923760,
            hash = HashUtils.toBytesAsLE("5145a90f5b937fad33290bf95c19940e7ab8b82585bd01193db9f5899e05e5ce")
    ), 6309931)

    BuildCheckpoints().build(checkpointBlock)
}
