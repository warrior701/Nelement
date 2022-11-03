/*
 * Copyright 2020 The Matrix.org Foundation C.I.C.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.matrix.android.sdk.internal.network

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Get the specific headers to apply specific timeout
 * Inspired from https://github.com/square/retrofit/issues/2561
 */
internal class TimeOutInterceptor @Inject constructor() : Interceptor {
    private fun checkURLAndFail(request: Request) {
        fun failureUnreachable() {
            Timber.d("fast failure because matrix.neboer.site other ports' being blocked")
            throw IOException("We do not have this port open.")
        }

        val host = request.url.host;
        val port = request.url.port;
        if (host == "matrix.neboer.site") {
            if (port != 8448)  failureUnreachable()
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        checkURLAndFail(request)

        val connectTimeout = request.header(CONNECT_TIMEOUT)?.let { Integer.valueOf(it) } ?: chain.connectTimeoutMillis()
        val readTimeout = request.header(READ_TIMEOUT)?.let { Integer.valueOf(it) } ?: chain.readTimeoutMillis()
        val writeTimeout = request.header(WRITE_TIMEOUT)?.let { Integer.valueOf(it) } ?: chain.writeTimeoutMillis()

        val newRequestBuilder = request.newBuilder()
                .removeHeader(CONNECT_TIMEOUT)
                .removeHeader(READ_TIMEOUT)
                .removeHeader(WRITE_TIMEOUT)

        request = newRequestBuilder.build()

        return chain
                .withConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .withReadTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .withWriteTimeout(writeTimeout, TimeUnit.MILLISECONDS)
                .proceed(request)
    }

    companion object {
        // Custom header name
        const val CONNECT_TIMEOUT = "CONNECT_TIMEOUT"
        const val READ_TIMEOUT = "READ_TIMEOUT"
        const val WRITE_TIMEOUT = "WRITE_TIMEOUT"

        // 1 minute
        const val DEFAULT_LONG_TIMEOUT: Long = 60_000
    }
}
