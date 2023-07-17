package io.horizontalsystems.bitcoincore.serializers;

import java.nio.ByteBuffer;

public class CompactSize {
    public int size;
    public int offset;

    public CompactSize(int size, int offset) {
        this.size = size;
        this.offset = offset;
    }

    public static CompactSize fromBuffer(byte[] data, int offset) {
        byte firstByte = data[offset];

        if (firstByte < 1) {
            new CompactSize(0, 0);
//            throw new IllegalArgumentException("Cannot read first byte because too small");
        }

        int size;
        int newOffset = offset + 1;

        switch (firstByte) {
            case (byte) 0xfd:
                size = ByteBuffer.wrap(data, newOffset, 2).getShort() & 0xffff;
                newOffset += 2;
                break;
            case (byte) 0xfe:
                size = ByteBuffer.wrap(data, newOffset, 4).getInt();
                newOffset += 4;
                break;
            case (byte) 0xff:
                size = (int) ByteBuffer.wrap(data, newOffset, 8).getLong();
                newOffset += 8;
                break;
            default:
                size = Byte.toUnsignedInt(firstByte);
                break;
        }

        int offsetDiff = newOffset - offset;

        return new CompactSize(size, offsetDiff);
    }

    public static byte[] fromSize(int size) {
        ByteBuffer buffer;

        if (size <= 252) {
            buffer = ByteBuffer.allocate(1);
            buffer.put((byte) size);
        } else if (size <= 65535) {
            buffer = ByteBuffer.allocate(3);
            buffer.put((byte) 0xfd);
            buffer.putShort((short) size);
        } else if (size <= 4294967295L) {
            buffer = ByteBuffer.allocate(5);
            buffer.put((byte) 0xfe);
            buffer.putInt(size);
        } else {
            buffer = ByteBuffer.allocate(9);
            buffer.put((byte) 0xff);
            buffer.putLong(size);
        }

        return buffer.array();
    }
}
