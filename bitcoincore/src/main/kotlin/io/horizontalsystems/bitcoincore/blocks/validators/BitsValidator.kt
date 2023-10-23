package io.horizontalsystems.bitcoincore.blocks.validators

import android.util.Log
import io.horizontalsystems.bitcoincore.models.Block

class BitsValidator : IBlockChainedValidator {

    override fun isBlockValidatable(block: Block, previousBlock: Block): Boolean {
        return true
    }

    override fun validate(block: Block, previousBlock: Block) {
        Log.e("Peer[", "BitsValidator block.bits=${block.bits}, prev=${previousBlock.bits}")
        if (block.bits != previousBlock.bits) {
//            throw BlockValidatorException.NotEqualBits()
        }
    }

}
