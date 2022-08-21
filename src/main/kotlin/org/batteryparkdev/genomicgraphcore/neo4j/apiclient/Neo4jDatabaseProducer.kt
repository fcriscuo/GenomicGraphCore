package org.batteryparkdev.genomicgraphcore.neo4j.apiclient


import org.batteryparkdev.genomicgraphcore.common.service.Neo4jPropertiesService
import org.neo4j.dbms.api.DatabaseManagementService
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder
import org.neo4j.graphdb.GraphDatabaseService
import java.io.File

object Neo4jDatabaseProducer {

    private val databaseDir: File = File(Neo4jPropertiesService.neo4jDatabasePath)
    val databaseName: String = Neo4jPropertiesService.neo4jDatabase

    private val graphdb = configureDatabase()

    fun produce(): GraphDatabaseService = graphdb

    private fun configureDatabase(): GraphDatabaseService {
        val managementService = DatabaseManagementServiceBuilder(databaseDir).build()
        registerShutdownHook(managementService)
        return managementService.database(databaseName)
    }

    private fun registerShutdownHook(managementService: DatabaseManagementService) {
        Runtime.getRuntime().addShutdownHook(Thread() {
            fun run() {
                managementService.shutdown()
            }
        })
    }
}

