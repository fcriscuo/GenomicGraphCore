package org.batteryparkdev.genomicgraphcore.common

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier


interface CoreModel
{
    abstract fun getNodeIdentifier(): NodeIdentifier

    abstract fun generateLoadModelCypher(): String

    abstract fun isValid(): Boolean

    abstract fun getPubMedIds(): List<Int>

}

interface CoreModelCreator {
    abstract val createCoreModelFunction: (CSVRecord) ->CoreModel

}
