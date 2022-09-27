package org.batteryparkdev.genomicgraphcore.common.datamining

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.genomicgraphcore.common.service.LogService
import java.io.File
import java.io.IOException
import java.net.URL

/**
 * Copied over from legacy project 2022Jul26
 */

data class HttpClientException(val response: HttpResponse) : IOException("HTTP Error ${response.status}")

fun loadFileFromUrl(fileName: String, url: String): Int {
    var lineCount = 0
    runBlocking {
        val client = HttpClient(Apache) {
            followRedirects = true
        }
        client.getAsFile(fileName, url) { file ->
            lineCount = file.readLines().size
            LogService.logInfo("File at ${file.absolutePath} line count = $lineCount" )

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

