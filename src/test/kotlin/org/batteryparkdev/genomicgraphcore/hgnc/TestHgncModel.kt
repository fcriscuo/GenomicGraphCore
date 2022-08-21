package org.batteryparkdev.genomicgraphcore.hgnc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.io.CSVRecordSupplier
import java.nio.file.Paths
import kotlin.streams.asSequence

class TestHgncModel {
    private var nodeCount = 0

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceCSVRecords(filename: String) =
        produce<CSVRecord> {
            val path = Paths.get(filename)
            CSVRecordSupplier(path).get()
                .asSequence()
                .forEach {
                    send(it)
                    delay(20)
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.generateHgncModels(records: ReceiveChannel<CSVRecord>) =
        produce<HgncModel> {
            for (record in records){
                val hgnc = HgncModel(record)
                if (hgnc.isValid()){
                    send(hgnc)
                }
                delay(20L)
            }
        }

    fun testHgncModel(filename: String) = runBlocking {
        val models = generateHgncModels(produceCSVRecords(filename))
        for (model in models) {
            nodeCount += 1
            if (nodeCount % 20 == 0 ) {
                println(model.generateLoadModelCypher())
            }
        }
        println("Approved HGNC record count = $nodeCount")
    }
}

fun main (args: Array<String>){
    val filename = if (args.isNotEmpty()) args[0] else "./data/sample_hgnc_set.tsv"
    TestHgncModel().testHgncModel(filename)
}