package com.anwang.safewallet.safekit.model

data class SafeInfo(
    val safe_usdt: String,
    val minamount: String,
    val eth: ChainInfo,
    val bsc: ChainInfo,
    val matic: MaticChainInfo
) {
    override fun toString(): String {
        return "SafeInfo(safe_usdt='$safe_usdt', minamount='$minamount', eth=$eth, bsc=$bsc)"
    }
}

data class ChainInfo(
    val price: String,
    val gas_price_gwei: String,
    val safe_fee: String,
    val safe2eth: Boolean,
    val eth2safe: Boolean
) {
    override fun toString(): String {
        return "ChainInfo(price='$price', gas_price_gwei='$gas_price_gwei', safe_fee='$safe_fee', safe2eth=$safe2eth, eth2safe=$eth2safe)"
    }
}

data class MaticChainInfo(
    val price: String,
    val gas_price_gwei: String,
    val safe_fee: String,
    val safe2matic: Boolean,
    val matic2safe: Boolean
) {
    override fun toString(): String {
        return "MaticChainInfo(price='$price', gas_price_gwei='$gas_price_gwei', safe_fee='$safe_fee', safe2matic=$safe2matic, matic2safe=$matic2safe)"
    }
}


