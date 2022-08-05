package com.rrawat.gitlabmrview.kotlin

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloHttpException
import com.apollographql.apollo3.network.okHttpClient
import com.rrawat.gitlabmrview.PipeLineQuery
import kotlinx.datetime.Instant
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager


class GitlabAPIException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    constructor(cause: Throwable) : this(null, cause)
}


class GitlabAPI(baseUrl: String) {

    private val apolloClient =
        ApolloClient.Builder().serverUrl("$baseUrl/api/graphql").okHttpClient(getUnsafeOkHttpClient()).build()


    suspend fun fetchPipelines(project: String): List<PipelineRecord> {
        val result = mutableListOf<PipelineRecord>()
        var cursor: String? = null
        try {
            while (true) {
                val response = apolloClient.query(PipeLineQuery(project, cursor)).execute()
                val projectData = response.data?.project ?: break
                for (item in projectData.mergeRequests?.edges ?: listOf()) {
                    result.add(
                        PipelineRecord(
                            item!!.node!!.iid,
                            projectData.name,
                            item.node!!.sourceBranch,
                            item.node.webUrl!!,
                            item.node.headPipeline?.let { Instant.parse(it.updatedAt as String) },
                            item.node.headPipeline?.status?.toString() ?: "",
                        )
                    )
                }
                if (projectData.mergeRequests!!.pageInfo.hasNextPage) {
                    cursor = projectData.mergeRequests.pageInfo.endCursor
                } else break
            }
        } catch (ex: ApolloHttpException) {
            println(ex)
            println(ex.body)
            throw GitlabAPIException("Server returns ${ex.statusCode}")
        }
        return result
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        val context = SSLContext.getInstance("TLS")
        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}

            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

        }
        context.init(null, arrayOf(trustManager), SecureRandom())

        return OkHttpClient.Builder().addInterceptor { chain ->
            println("Requesting: ${chain.request().url}")
            val header = when (auth.first) {
                AuthMethod.PRIVATE_ACCESS_TOKEN -> "PRIVATE-TOKEN"
                AuthMethod.OAUTH2 -> "Authorization"
            }
            val request = chain.request().newBuilder().addHeader(header, auth.second).build()
            chain.proceed(request)
        }.sslSocketFactory(context.socketFactory, trustManager).hostnameVerifier { _, _ -> true }.build()
    }

    enum class AuthMethod { PRIVATE_ACCESS_TOKEN, OAUTH2 }

    private var auth = Pair(AuthMethod.PRIVATE_ACCESS_TOKEN, "")

    fun setPersonalAccessToken(token: String) {
        auth = Pair(AuthMethod.PRIVATE_ACCESS_TOKEN, token)
    }


}