package io.horizontalsystems.bitcoincore.utils

import android.annotation.SuppressLint
import io.horizontalsystems.bitcoincore.extensions.toHexString
import okhttp3.OkHttpClient
import java.io.DataInputStream
import java.io.IOException
import java.net.*
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object NetworkUtils {

    fun getLocalInetAddress(): InetAddress {
        try {
            return InetAddress.getLocalHost()
        } catch (e: UnknownHostException) {
            throw RuntimeException(e)
        }
    }

    fun getIPv6(inetAddr: InetAddress): ByteArray {
        val ip = inetAddr.address
        if (ip.size == 16) {
            return ip
        }

        if (ip.size == 4) {
            val ipv6 = ByteArray(16)
            ipv6[10] = -1
            ipv6[11] = -1
            System.arraycopy(ip, 0, ipv6, 12, 4)
            return ipv6
        }

        throw RuntimeException("Bad IP: " + ip.toHexString())
    }

    fun socksProxyAddress(): InetSocketAddress? = try {
        requireSocksProxyAddress()
    } catch (e: IOException) {
        null
    }

    /**
     * Returns null when no SOCKS proxy is set, and throws when one is set but incomplete or
     * invalid, so callers that must not bypass the proxy can tell the two apart.
     */
    fun requireSocksProxyAddress(): InetSocketAddress? {
        val socksProxyHost = System.getProperty("socksProxyHost")
        val socksProxyPortValue = System.getProperty("socksProxyPort")
        if (socksProxyHost == null && socksProxyPortValue == null) return null

        val socksProxyPort = socksProxyPortValue?.toIntOrNull()?.takeIf { it in 0..65535 }
        if (socksProxyHost == null || socksProxyPort == null) {
            throw IOException("Invalid SOCKS proxy configuration: host=$socksProxyHost, port=$socksProxyPortValue")
        }

        return InetSocketAddress.createUnresolved(socksProxyHost, socksProxyPort)
    }

    fun createSocket(): Socket {
        val socketAddress = socksProxyAddress()

        return if (socketAddress != null) {
            val proxy = Proxy(Proxy.Type.SOCKS, socketAddress)
            Socket(proxy)
        } else {
            Socket()
        }
    }

    /**
     * Resolves [host] to one IPv4 address through Tor's SOCKS RESOLVE extension (command 0xF0),
     * so the lookup never reaches the local DNS resolver. Only Tor implements it.
     */
    fun resolveThroughSocks(host: String, proxy: InetSocketAddress, timeoutMillis: Int = 30_000): String? {
        val hostBytes = host.toByteArray(Charsets.US_ASCII)
        require(hostBytes.size <= 255) { "Host name too long: $host" }

        Socket().use { socket ->
            socket.connect(InetSocketAddress(proxy.hostString, proxy.port), timeoutMillis)
            socket.soTimeout = timeoutMillis
            val output = socket.getOutputStream()
            val input = DataInputStream(socket.getInputStream())

            output.write(byteArrayOf(0x05, 0x01, 0x00))
            output.flush()
            val greeting = ByteArray(2).also { input.readFully(it) }
            if (greeting[0] != 0x05.toByte() || greeting[1] != 0x00.toByte()) return null

            output.write(byteArrayOf(0x05, 0xF0.toByte(), 0x00, 0x03, hostBytes.size.toByte()) + hostBytes + byteArrayOf(0x00, 0x00))
            output.flush()

            val header = ByteArray(4).also { input.readFully(it) }
            if (header[1] != 0x00.toByte()) return null

            return when (header[3]) {
                0x01.toByte() -> {
                    val address = ByteArray(4).also { input.readFully(it) }
                    InetAddress.getByAddress(address).hostAddress
                }
                else -> null
            }
        }
    }

    @SuppressLint("TrustAllX509TrustManager", "BadHostnameVerifier")
    fun getUnsafeOkHttpClient(): OkHttpClient {
        return try {
            val trustAllCerts = arrayOf<TrustManager>(
                    object : X509TrustManager {
                        @Throws(CertificateException::class)
                        override fun checkClientTrusted(chain: Array<X509Certificate>,
                                                        authType: String) {
                        }

                        @Throws(CertificateException::class)
                        override fun checkServerTrusted(chain: Array<X509Certificate>,
                                                        authType: String) {
                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate> {
                            return arrayOf()
                        }
                    }
            )
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory = sslContext.socketFactory
            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, (trustAllCerts[0] as X509TrustManager))
            builder.hostnameVerifier(HostnameVerifier { _, _ -> true })
            builder.connectTimeout(5000, TimeUnit.MILLISECONDS)
            builder.readTimeout(60000, TimeUnit.MILLISECONDS)
//            builder.proxy(Proxy( Proxy.Type.HTTP , InetSocketAddress("47.89.208.160", 58972) ))
            builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

}
