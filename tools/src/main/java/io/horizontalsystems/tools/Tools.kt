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
            previousBlockHeaderHash = HashUtils.toBytesAsLE("a02922942fd13752a8e27e98446aacbaaa78b80fa84b949c55fd78472f41aac0"),
            merkleRoot = HashUtils.toBytesAsLE("7475bcc8325f378b1ee90be1f6794f4fc9d5b088655a891f7eaea90ceac7180c"),
            timestamp = 1676087124,
            bits = 0,
            nonce = 106743180,
            hash = HashUtils.toBytesAsLE("7ab06750527c8d137ccd8c425107110dea3c82c8b0541234c6384f05865609a9")
    ), 4609314)

    BuildCheckpoints().build(checkpointBlock)
}
