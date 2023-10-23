package io.horizontalsystems.bitcoincore.serializers

import android.util.Log
import io.horizontalsystems.bitcoincore.core.IHasher
import io.horizontalsystems.bitcoincore.extensions.toHexString
import io.horizontalsystems.bitcoincore.io.BitcoinInputMarkable
import io.horizontalsystems.bitcoincore.storage.BlockHeader

class BlockHeaderParser(private val hasher: IHasher) {

    fun parse(input: BitcoinInputMarkable): BlockHeader {
        input.mark()
        val bytes = input.readBytes(input.count)
        val headerOffset = DogeHeaderParser.decodeHeader(bytes)
        Log.e("Peer[", "head count=$headerOffset")
        input.reset()
//        val header = input.readBytes(4).toHexString()
//        input.reset()
        input.mark()
        // dogecoin
        val payload = input.readBytes(80)
        val hash = hasher.hash(payload)
        input.reset()
        Log.w("Peer[", "header=, input count=${input.count}")
        val version = input.readInt()
        val previousBlockHeaderHash = input.readBytes(32)
        val merkleRoot = input.readBytes(32)
        val timestamp = input.readUnsignedInt()
        val bits = input.readUnsignedInt()
        val nonce = input.readUnsignedInt()

        if (headerOffset != -1 && headerOffset < input.count) {
            input.readBytes(headerOffset - 80)
            Log.w("Peer[", "other length=${headerOffset - 80}")
        }
        Log.e("Peer[", "hash=${hash.toHexString()}, version=$version, previousBlockHeaderHash=${previousBlockHeaderHash.toHexString()}, merkleRoot=${merkleRoot.toHexString()}, timestamp=$timestamp, bits=$bits, nonce=${nonce}")
        return BlockHeader(version, previousBlockHeaderHash, merkleRoot, timestamp, bits, nonce, hash)
    }

    /*fun parseDoge(input: BitcoinInputMarkable): BlockHeader {

    }*/

}
