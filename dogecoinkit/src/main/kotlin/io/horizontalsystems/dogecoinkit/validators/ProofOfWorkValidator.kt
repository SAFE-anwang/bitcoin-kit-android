package io.horizontalsystems.dogecoinkit.validators

import android.util.Log
import io.horizontalsystems.bitcoincore.blocks.validators.BlockValidatorException
import io.horizontalsystems.bitcoincore.blocks.validators.IBlockChainedValidator
import io.horizontalsystems.bitcoincore.crypto.CompactBits
import io.horizontalsystems.bitcoincore.extensions.toHexString
import io.horizontalsystems.bitcoincore.extensions.toReversedHex
import io.horizontalsystems.bitcoincore.io.BitcoinOutput
import io.horizontalsystems.bitcoincore.models.Block
import io.horizontalsystems.dogecoinkit.ScryptHasher
import java.math.BigInteger

class ProofOfWorkValidator(private val scryptHasher: ScryptHasher) : IBlockChainedValidator {

    override fun validate(block: Block, previousBlock: Block) {
//        Log.e("Peer[", "ProofOfWorkValidator ${block.bits}, ${block.headerHash.toReversedHex()}, ${CompactBits.decode(block.bits)}")
        /*check(BigInteger(block.headerHash.toReversedHex(), 16) < CompactBits.decode(block.bits)) {
            throw BlockValidatorException.InvalidProofOfWork()
        }*/
        val blockHeaderData = getSerializedBlockHeader(block)

        val powHash = scryptHasher.hash(blockHeaderData).toHexString()
        Log.e("Peer[", "ProofOfWorkValidator ${block.bits}, ${BigInteger(powHash, 16)}, ${CompactBits.decode(block.bits)}")
        /*check(BigInteger(powHash, 16) < CompactBits.decode(block.bits)) {
            throw BlockValidatorException.InvalidProofOfWork()
        }*/
    }

    private fun getSerializedBlockHeader(block: Block): ByteArray {
        Log.e("Peer[", "ProofOfWorkValidator block ${block.version},${block.previousBlockHash.toHexString()},${block.merkleRoot},${block.timestamp},${block.bits},${block.nonce}")
        return BitcoinOutput()
                .writeInt(block.version)
                .write(block.previousBlockHash)
                .write(block.merkleRoot)
                .writeUnsignedInt(block.timestamp)
                .writeUnsignedInt(block.bits)
                .writeUnsignedInt(block.nonce)
                .toByteArray()
    }

    override fun isBlockValidatable(block: Block, previousBlock: Block): Boolean {
        return true
    }

}
