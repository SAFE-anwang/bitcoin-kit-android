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
            previousBlockHeaderHash = HashUtils.toBytesAsLE("c9762befcb05891026a6f207787029095f914c07f9d4213df568c109fab7c06e"),
            merkleRoot = HashUtils.toBytesAsLE("1bd67caa4fc7486e64dfd0fdd17800507c32687ddc176a099780d441e77b1465"),
            timestamp = 1658649095,
            bits = 0,
            nonce = 304200,
            hash = HashUtils.toBytesAsLE("01c818bd957bc9fe0a2004cdb62bd12d3aec0691e5eb6bc36e47bcd3db7d693f")
    ), 10529)

    BuildCheckpoints().build(checkpointBlock)
}
