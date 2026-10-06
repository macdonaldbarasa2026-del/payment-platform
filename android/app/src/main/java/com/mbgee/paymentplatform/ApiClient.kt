package com.mbgee.paymentplatform

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class ApiClient(
    private val baseUrl: String,
    private val token: String? = null
) {
    data class Result(
        val status: Int,
        val body: JSONObject
    )

    private fun call(
        method: String,
        path: String,
        body: JSONObject? = null
    ): Result {
        val connection =
            URL(baseUrl.trimEnd('/') + path)
                .openConnection() as HttpURLConnection

        connection.requestMethod = method
        connection.connectTimeout = 15000
        connection.readTimeout = 20000

        connection.setRequestProperty(
            "Accept",
            "application/json"
        )

        token?.let {
            connection.setRequestProperty(
                "Authorization",
                "Bearer $it"
            )
        }

        if (body != null) {
            connection.doOutput = true
            connection.setRequestProperty(
                "Content-Type",
                "application/json"
            )

            OutputStreamWriter(
                connection.outputStream
            ).use {
                it.write(body.toString())
            }
        }

        return try {
            val status = connection.responseCode

            val stream =
                if (status in 200..299)
                    connection.inputStream
                else
                    connection.errorStream
                        ?: connection.inputStream

            val text =
                BufferedReader(
                    InputStreamReader(stream)
                ).use { it.readText() }

            Result(
                status,
                if (text.isBlank())
                    JSONObject()
                else
                    JSONObject(text)
            )
        } finally {
            connection.disconnect()
        }
    }

    fun login(
        email: String,
        password: String
    ) =
        call(
            "POST",
            "/v1/auth/login",
            JSONObject()
                .put("email", email)
                .put("password", password)
        )

    fun register(
        name: String,
        email: String,
        password: String
    ) =
        call(
            "POST",
            "/v1/auth/register",
            JSONObject()
                .put("name", name)
                .put("email", email)
                .put("password", password)
        )

    fun projects() =
        call("GET", "/v1/projects")

    fun createProject(
        name: String,
        description: String
    ) =
        call(
            "POST",
            "/v1/projects",
            JSONObject()
                .put("name", name)
                .put("description", description)
        )

    fun keys(projectId: String) =
        call(
            "GET",
            "/v1/projects/" +
                URLEncoder.encode(
                    projectId,
                    "UTF-8"
                ) +
                "/keys"
        )

    fun createKey(
        projectId: String,
        environment: String
    ) =
        call(
            "POST",
            "/v1/projects/" +
                URLEncoder.encode(
                    projectId,
                    "UTF-8"
                ) +
                "/keys",
            JSONObject()
                .put(
                    "environment",
                    environment
                )
        )

    fun payments(projectId: String) =
        call(
            "GET",
            "/v1/projects/" +
                URLEncoder.encode(
                    projectId,
                    "UTF-8"
                ) +
                "/payments"
        )
}
