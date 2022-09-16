package org.batteryparkdev.genomicgraphcore.hgnc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelCreator
import org.batteryparkdev.genomicgraphcore.common.TestCoreModel
import org.batteryparkdev.genomicgraphcore.common.io.CSVRecordSupplier
import java.nio.file.Paths
import kotlin.streams.asSequence

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else
        "./data/small_hgnc_set.tsv"
    TestHgncModel(HgncModel.Companion).loadModels(filename)
}

class TestHgncModel(val creator: CoreModelCreator) {
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
    fun CoroutineScope.generateModels(records: ReceiveChannel<CSVRecord>) =
        produce<CoreModel> {
            for (record in records){
               val model = creator.createCoreModelFunction(record)
                if (model.isValid()) {
                    send(model)
                }
                delay(20L)
            }
        }
    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.generateCypher(models: ReceiveChannel<CoreModel>) =
        produce<String> {
            for (model in models){
                if (model.isValid()) {
                    send(model.generateLoadModelCypher())
                    delay(20)
                }
            }
        }


    fun loadModels(filename: String) = runBlocking {
        val cyphers = generateCypher(generateModels(produceCSVRecords(filename)))
        for (cypher in cyphers) {
            nodeCount += 1
            println(cypher)
        }
        println("Loaded record count for ${creator::class.java.name } = $nodeCount")
    }
}
