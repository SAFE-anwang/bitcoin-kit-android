package io.horizontalsystems.bitcoincore.serializers;

import android.util.Log;

public class DogeHeaderParser {

    public static int decodeHeader(byte[] payload) {
        int offset = -1;
        CompactSize compactSize;
        Log.e("Peer[", "head start " + payload[1] + " " + payload[2] + " " + payload[3]);
        if (payload[1] == 0x01 && payload[2] == 0x62 && payload[3] == 0x00) {
            offset = 0;
            // Merged mining block header

            // Normal header
            offset += 80;

            // Version parent block
            offset += 4;

            compactSize = CompactSize.fromBuffer(payload, offset);
            offset += compactSize.offset;

            // tx_in
            for (int j = 0; j < compactSize.size; j++) {
                offset += 36;

                CompactSize compactSizeTxIn = CompactSize.fromBuffer(payload, offset);
                offset += compactSizeTxIn.offset + compactSizeTxIn.size + 4;
            }

            // tx_out
            compactSize = CompactSize.fromBuffer(payload, offset);
            offset += compactSize.offset;

            for (int j = 0; j < compactSize.size; j++) {
                offset += 8;

                CompactSize compactSizeTxOut = CompactSize.fromBuffer(payload, offset);
                offset += compactSizeTxOut.offset + compactSizeTxOut.size;
            }

            // locktime + hash
            offset += 4 + 32;

            // Coinbase Branch: Merkle branch
            compactSize = CompactSize.fromBuffer(payload, offset);
            offset += compactSize.offset + compactSize.size * 32;

            // branch side mask
            offset += 4;

            // Blockchain Branch: Merkle branch
            compactSize = CompactSize.fromBuffer(payload, offset);
            offset += compactSize.offset + compactSize.size * 32;

            // branch side mask
            offset += 4;

            // parent block header
            offset += 80;

//            merkleBlock.blockHeader = new byte[offset];
//            System.arraycopy(payload, 0, merkleBlock.blockHeader, 0, offset);
        }
        return offset;
    }

}
