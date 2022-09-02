package org.batteryparkdev.genomicgraphcore.common

import org.apache.commons.csv.CSVRecord
import org.apache.commons.lang3.RandomStringUtils
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier

interface CoreModel {
    abstract fun getNodeIdentifier(): NodeIdentifier

    abstract fun generateLoadModelCypher(): String

    abstract fun isValid(): Boolean

    abstract fun getPubMedIds(): List<Int>

    abstract fun getModelGeneSymbol(): String

    abstract fun getModelSampleId(): String   // support for alphanumeric sample identifiers


    /*
Function to generate Cypher commands to create a
GeneMutationCollection - [HAS_MUTATION] -> specific Mutation relationship
n.b. It's possible to establish >1 gene-mutation relationship in the same Cypher transaction,
 so the relationship name must be unique
*/
    fun generateGeneMutationCollectionRelationshipCypher( nodename: String): String {
        val relationship = "HAS_".plus(nodename.uppercase())
        val suffix = RandomStringUtils.randomAlphanumeric(6).lowercase()
        val gene_rel_name = "gene_mut_rel_".plus(suffix)
        val gene_coll_name = "gene_mut_coll_".plus(suffix)
        return "CALL apoc.merge.node([\"GeneMutationCollection\"], " +
                " {gene_symbol: ${getModelGeneSymbol().formatNeo4jPropertyValue()}}, " +
                "{},{} ) YIELD node AS $gene_coll_name \n " +
                " CALL apoc.merge.relationship( $gene_coll_name, '$relationship', " +
                " {}, {}, $nodename) YIELD rel AS $gene_rel_name \n"
    }

    fun generateSampleMutationCollectionRelationshipCypher( nodename: String): String {
        val relationship = "HAS_".plus(nodename.uppercase())
        return "CALL apoc.merge.node([\"SampleMutationCollection\"], " +
                " {sample_id: ${getModelSampleId()}}, " +
                "{},{} ) YIELD node AS  sample_mut_coll \n " +
                " CALL apoc.merge.relationship( sample_mut_coll, '$relationship', " +
                " {}, {}, $nodename) YIELD rel AS sample_mut_rel \n"
    }
}

interface CoreModelCreator {
    abstract val createCoreModelFunction: (CSVRecord) -> CoreModel
}

interface CoreModelDao{
    abstract val modelRelationshipFunctions: (CoreModel) -> Unit
}
