package com.anwang.safewallet.safekit.netwok

import com.anwang.safewallet.safekit.model.SafeInfo
import com.anwang.safewallet.safekit.model.SafeUsdtInfo
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

class SafeProvider(baseUrl: String) {

    private val service by lazy {
        RetrofitUtils.build(baseUrl)
            .create(SafeService::class.java)
    }

    fun getSafeInfo(netType: String): Single<SafeInfo> {
        return service.getSafeInfo(netType)
    }

    fun getSafeUsdtInfo(netType: String): Single<SafeUsdtInfo> {
        return service.getSafeUsdtInfo(netType)
    }

    private interface SafeService {
        @GET("v1/gate/{netType}")
        fun getSafeInfo(@Path("netType")netType: String): Single<SafeInfo>

        @GET("v1/gate/{netType}")
        fun getSafeUsdtInfo(@Path("netType")netType: String): Single<SafeUsdtInfo>
    }
}
