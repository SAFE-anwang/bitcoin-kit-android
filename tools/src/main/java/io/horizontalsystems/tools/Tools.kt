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
            previousBlockHeaderHash = HashUtils.toBytesAsLE("a609e9f3879149e1b3e46a10d59cca5b92ab8c399736b1997e87771a738df7c3"),
            merkleRoot = HashUtils.toBytesAsLE("b1bfdfa8c592d7d3c6ff6dad3ff3d07bf6b59afa8b0d287053ca514248fd189e"),
            timestamp = 1701440154,
            bits = 0,
            nonce = 129738930,
            hash = HashUtils.toBytesAsLE("3a9ab9b98897756c4ac37919b079b583d5c35e0940c98dc9019ec07f9789d368")
    ), 5375839)

    BuildCheckpoints().build(checkpointBlock)
}
