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
            previousBlockHeaderHash = HashUtils.toBytesAsLE("4f6b7c5f4f9b298fa0d6211d17dc252b92e0d5ab45c65c121f1d6c5fd7b82de2"),
            merkleRoot = HashUtils.toBytesAsLE("87a5518e342bef89de463241f9ab9d1902ba13b9bc3b4078945c53fe1500e8b4"),
            timestamp = 1702211064,
            bits = 0,
            nonce = 129628890,
            hash = HashUtils.toBytesAsLE("0ca19461c59e31b4d63d21f0b909cf26c46e421d99c01a078a00bc5df472b16a")
    ), 5400104)

    BuildCheckpoints().build(checkpointBlock)
}
