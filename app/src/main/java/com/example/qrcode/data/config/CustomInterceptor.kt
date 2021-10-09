package com.example.qrcode.data.config

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomInterceptor @Inject constructor() : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        /*
    chain.request() returns original request that you can work with(modify, rewrite)
    */
        val request = chain.request()

        // Here you can rewrite the request

        /*
    chain.proceed(request) is the call which will initiate the HTTP work. This call invokes the request and returns the response as per the request.
        */

        //Here you can rewrite/modify the response
        return chain.proceed(request)
    }
}