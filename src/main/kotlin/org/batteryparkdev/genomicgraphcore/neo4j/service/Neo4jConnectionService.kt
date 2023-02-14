/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.batteryparkdev.genomicgraphcore.neo4j.service

import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.common.service.LogService
import org.batteryparkdev.genomicgraphcore.common.service.Neo4jPropertiesService
import org.neo4j.driver.*
import java.io.File

/**
 * Responsible for establishing a connection to a local or remote Neo4j database
 * Executes supplied Cypher commands
 *
 * Created by fcriscuo on 2021Aug06
 */
object Neo4jConnectionService {

    private val uri = Neo4jPropertiesService.neo4jUri
    private val logCypher = Neo4jPropertiesService.logCypherCommands
    //private val config: Config = Config.builder().withLogging(Logging.slf4j()).build()
    private val config: Config = Config.builder().build()
    private val database = Neo4jPropertiesService.neo4jDatabase
    private val driver = GraphDatabase.driver(
        uri, AuthTokens.basic(Neo4jPropertiesService.neo4jAccount, Neo4jPropertiesService.neo4jPassword),
        config
    )

    fun close() {
        driver.close()
        Neo4jCypherWriter.close()
    }

    fun isTestingContext(): Boolean = database in listOf ("sample","test")

    // expose the current database name
    fun getDatabaseName(): String = database

    // provide access to the session
    // should be used within a Java try or Kotlin useclause
    fun getSession() = driver.session(SessionConfig.forDatabase(database))

    /*
    Constraint definitions do not return a result
     */
    fun defineDatabaseConstraint(command: String) {
        val session: Session = driver.session(SessionConfig.forDatabase(database))
        session.use {
            session.writeTransaction { tx ->
                tx.run(command)
            }!!
        }
    }
    /*
    Function to execute a specified CQL file that alters the Neo4j
     */

    fun executeCqlSchemaFile(cqlFile: String): Unit {
        val file = File(cqlFile)
       // require(file.exists())
        require(file.extension == "cql")
        executeCypherCommand("CALL apoc.cypher.runSchemaFile(${cqlFile.formatNeo4jPropertyValue()})")
    }

    /*
    Function to execute a specified CQL file
     */
    fun executeCqlFIle(cqlFile: String):Unit {
       executeCypherCommand("CALL apoc.cypher.runFile(${cqlFile.formatNeo4jPropertyValue()})")
    }

    /*
    Function to execute a query that returns multiple results
    Return type is a List of Records
     */
    fun executeCypherQuery(query: String): List<Record> {
        val retList = mutableListOf<Record>()
        val session = driver.session(SessionConfig.forDatabase(database))
        session.use {
            try {
                session.readTransaction { tx ->
                    val result = tx.run(query)
                    while (result.hasNext()) {
                        retList.add(result.next())
                    }
                }
            } catch (e: Exception) {
                LogService.exception(e)
                LogService.error("Cypher query: $query")
            }
            return retList.toList()
        }
    }

    /*
    Function to asynchronously execute a Cypher load operation
    Intended to represent the receiver of a Kotlin channel
     */

    fun executeCypherLoadAsync(command: String): Unit {
        if (logCypher) {
            Neo4jCypherWriter.recordCypherCommand(command)
        }
        val session = driver.session(SessionConfig.forDatabase(database))
        session.use {
            try {
                session.writeTransaction { tx ->
                    tx.run(command)
                }!!
            } catch (e: Exception) {
                LogService.exception(e)
                LogService.error("Cypher command: $command")
            }
        }
    }

    fun executeCypherCommand(command: String): String {
        if (logCypher) {
            Neo4jCypherWriter.recordCypherCommand(command)
        }
        val session = driver.session(SessionConfig.forDatabase(database))
        lateinit var resultString: String
        session.use {
            try {
                session.writeTransaction { tx ->
                    val result: org.neo4j.driver.Result = tx.run(command)
                    resultString = when (result.hasNext()) {
                        true -> result.single()[0].toString()
                        false -> ""
                    }
                }!!
                return resultString.toString()
            } catch (e: Exception) {
                LogService.exception(e)
                LogService.error("Cypher command: $command")
            }
        }
        return ""
    }
}

