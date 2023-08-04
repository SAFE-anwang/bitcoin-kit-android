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
            version = 6422788,
            previousBlockHeaderHash = HashUtils.toBytesAsLE("19c4e740302b8761e2e20b4edb71ff5015b49a53e261ca5a83c3a9227729faa4"),
            merkleRoot = HashUtils.toBytesAsLE("3568b15113142af64ba4eb5c536aa25da84e993d753797ab5c0bb2fec7dd58bb"),
            timestamp = 1690179853,
            bits = 0,
            nonce = 0,
            hash = HashUtils.toBytesAsLE("394932a5df926f71582229bdc533108d4396c056d8291a8c7cf4396c6ac5a74f")
    ), 5194611)

    /*val checkpointBlock = Block(BlockHeader(
            version = 6422788,
            previousBlockHeaderHash = HashUtils.toBytesAsLE("ef26bb216f3720451368d3dbc83d5d9a2ee380bca287c31d4671d00771c93c42"),
            merkleRoot = HashUtils.toBytesAsLE("e11952dc9499338b0198c47d925d0fb0a9a31318af2c03712afe130be0b4aa0b"),
            timestamp = 1657646280,
            bits = 436421877,
            nonce = 0,
            hash = HashUtils.toBytesAsLE("ed7d266dcbd8bb8af80f9ccb8deb3e18f9cc3f6972912680feeb37b090f8cee0")
    ), 4303965)*/

    BuildCheckpoints().build(checkpointBlock)
}
