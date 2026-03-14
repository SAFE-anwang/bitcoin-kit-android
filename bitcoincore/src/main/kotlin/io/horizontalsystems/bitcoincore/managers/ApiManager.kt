package io.horizontalsystems.bitcoincore.managers

import android.annotation.SuppressLint
import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonValue
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedOutputStream
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class ApiManager(private val host: String) {
    private val logger = Logger.getLogger("ApiManager")

    @Throws
    fun get(resource: String): InputStream? {
        val url = "$host/$resource"

        logger.info("Fetching $url")

        return try {
            URL(url)
                    .openConnection()
                    .apply {
                        connectTimeout = 5000
                        readTimeout = 60000
                        setRequestProperty("Accept", "application/json")
                        setRequestProperty("content-type", "application/json")
                    }.getInputStream()
        } catch (exception: IOException) {
            throw ApiManagerException.Other("${exception.javaClass.simpleName}: $host")
        }
    }

    @Throws
    fun post(resource: String, data: String): JsonValue {
        try {
            val path = "$host/$resource"

            logger.info("Fetching $path")

            val url = URL(path)
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "POST"
            urlConnection.setRequestProperty("Content-Type", "application/json")
            val out = BufferedOutputStream(urlConnection.outputStream)
            val writer = BufferedWriter(OutputStreamWriter(out, "UTF-8"))
            writer.write(data)
            writer.flush()
            writer.close()
            out.close()

            return urlConnection.inputStream.use {
                Json.parse(it.bufferedReader())
            }
        } catch (exception: IOException) {
            throw ApiManagerException.Other("${exception.javaClass.simpleName}: $host")
        }
    }

    fun doOkHttpGet(uri: String): JsonValue {

        val url = "$host/$uri"
        logger.info("Fetching $url")

        try {
            val builder = OkHttpClient.Builder()
                .apply {
                    connectTimeout(5000, TimeUnit.MILLISECONDS)
                    readTimeout(60000, TimeUnit.MILLISECONDS)
                }

            setUnsafeSocketFactory(builder)
            val httpClient: OkHttpClient = builder.build()
            httpClient.newCall(Request.Builder().url(url).build())
                    .execute()
                    .use { response ->

                        if (response.isSuccessful) {
                            response.body?.let {
                                return Json.parse(it.string())
                            }
                        }

                    if (response.code == 404) {
                        throw ApiManagerException.Http404Exception
                    } else {
                        throw ApiManagerException.Other("Unexpected Error:$response")
                    }
                }
        } catch (e: ApiManagerException) {
            logger.info("Fetching error $e")
            throw e
        }
        catch (e: Exception) {
            logger.info("Fetching error2 $e")
            throw ApiManagerException.Other("${e.javaClass.simpleName}: $host, ${e.localizedMessage}")
        }
    }


    @SuppressLint("TrustAllX509TrustManager", "BadHostnameVerifier")
    private fun setUnsafeSocketFactory(builder: OkHttpClient.Builder) {
        try {
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
            )
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory = sslContext.socketFactory
            builder.sslSocketFactory(sslSocketFactory, (trustAllCerts[0] as X509TrustManager))
            builder.hostnameVerifier(HostnameVerifier { _, _ -> true })
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}

sealed class ApiManagerException : Exception() {
    object Http404Exception : ApiManagerException()
    class Other(override val message: String) : ApiManagerException()
}
