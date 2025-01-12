package io.horizontalsystems.tools

import io.horizontalsystems.bitcoincore.models.Block
import io.horizontalsystems.bitcoincore.storage.BlockHeader
import io.horizontalsystems.bitcoincore.utils.HashUtils
import java.util.logging.Level
import java.util.logging.Logger

// Go to
// Edit Configurations... -> ToolsKt -> VM Options
// And paste the following
// -classpath $Classpath$:bitcoincashkit/src/main/resources:bitcoinkit/src/main/resources:dashkit/src/main/resources:ecashkit/src/main/resources:litecoinkit/src/main/resources
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
            version = 6422788,
            previousBlockHeaderHash = HashUtils.toBytesAsLE("463cb7ec321eee93a08fdba2b6a1da7f5b4769f7b057b9db975e5ce0a561dfd9"),
            merkleRoot = HashUtils.toBytesAsLE("eac169f1cd941bbbdf7e2f5d0f99ec26469dea3162252b0b07831d676ce895fc"),
            timestamp = 1713042240,
            bits = 436272948,
            nonce = 0,
            hash = HashUtils.toBytesAsLE("29a874a3accf4d3ae1a695e4761d6bb2e3e5bc4116682c79321e8262eac0ea35")
    ), 5170539)

    BuildCheckpoints().build(checkpointBlock)
}
