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
            version = 1,
            previousBlockHeaderHash = HashUtils.toBytesAsLE("1a91e3dace36e2be3bf030a65679fe821aa1d6ef92e7c9902eb318182c355691"),
            merkleRoot = HashUtils.toBytesAsLE("5f7e779f7600f54e528686e91d5891f3ae226ee907f461692519e549105f521c"),
            timestamp = 1386489327,
            bits = 504365040,
            nonce = 1417875456,
            hash = HashUtils.toBytesAsLE("82bc68038f6034c0596b6e313729793a887fded6e92a31fbdf70863f89d9bea2")
    ), 1)

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
