package org.batteryparkdev.genomicgraphcore.common.datamining

import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import io.ktor.client.engine.apache.Apache
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.io.File
import java.io.IOException
import java.net.URL

/**
 * Copied over from legacy project 2022Jul26
 */
private val logger = KotlinLogging.logger {}

data class HttpClientException(val response: HttpResponse) : IOException("HTTP Error ${response.status}")

fun loadFileFromUrl(fileName: String, url: String): Int {
    var lineCount = 0
    runBlocking {
        val client = HttpClient(Apache) {
            followRedirects = true
        }
        client.getAsFile(fileName, url) { file ->
            lineCount = file.readLines().size
            logger.info { "File at ${file.absolutePath} line count = $lineCount" }

        }
    }
    return lineCount
}

private suspend fun HttpClient.getAsFile(fileName: String, url: String, callback: suspend (file: File) -> Unit) {
    val file = getAsFile(fileName, url)
    try {
        callback(file)
    } finally {
        //file.delete()
    }
}

private suspend fun HttpClient.getAsFile(fileName: String, url: String): File {
    val file = File(fileName)
    val response = request<HttpResponse> {
        url(URL(url))
        method = HttpMethod.Get
    }
    if (!response.status.isSuccess()) {
        throw HttpClientException(response)
    }
    response.content.copyAndClose(file.writeChannel())
    return file
}