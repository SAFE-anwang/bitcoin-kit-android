package io.horizontalsystems.hodler

import io.horizontalsystems.bitcoincore.utils.Utils

enum class LockTimeInterval(private val value: Int) {
//    hour(7),
//    month(5063),     //  30 * 24 * 60 * 60 / 512
//    halfYear(30881), // 183 * 24 * 60 * 60 / 512
//    year(61593),     // 365 * 24 * 60 * 60 / 512

    month_1(1),
    month_3(3),
    month_6(6),
    month_9(9),
    year_1(12),
    year_3(36),
    year_5(60),
    year_10(120);

    private val sequenceTimeSecondsGranularity = 512
    private val relativeLockTimeLockMask = 0x400000 // (1 << 22)

    // need to write to extra data output as 2 bytes
    val valueAs2BytesLE: ByteArray = Utils.intToByteArray(value).reversedArray().copyOfRange(0, 2)
    val valueInSeconds: Int = value * sequenceTimeSecondsGranularity

    val sequenceNumber: Int = relativeLockTimeLockMask or value
    val sequenceNumberAs3BytesLE: ByteArray = Utils.intToByteArray(sequenceNumber).reversedArray().copyOfRange(0, 3)

    fun serialize(): String {
        return value.toString()
    }

    fun value(): Int{
        return value;
    }

    companion object {
        fun deserialize(serialized: String): LockTimeInterval? {
            return fromValue(serialized.toInt())
        }

        fun from2BytesLE(bytes: ByteArray): LockTimeInterval? {
            if (bytes.size != 2) return null

            return fromValue(Utils.byteArrayToUInt16LE(bytes))
        }

        private fun fromValue(value: Int): LockTimeInterval? {
            return values().find {
                it.value == value
            }
        }
    }
}
