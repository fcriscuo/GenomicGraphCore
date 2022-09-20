package org.batteryparkdev.genomicgraphcore.uniprot.dao

import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelDao
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.uniprot.model.UniprotModel
import org.batteryparkdev.genomicgraphcore.common.parseToNeo4jStringList

class UniprotModelDao (private val uniprotModel: UniprotModel) {

    fun generateUniprotCypher(): String = generateMergeCypher()
        .plus(" RETURN  ${UniprotModel.nodename} \n")

    private fun generateMergeCypher() : String  =
        " CALL apoc.merge.node(['UniProt'], {entry_id: ${uniprotModel.entryId.formatNeo4jPropertyValue()} }," +
                "{ entry_name: ${uniprotModel.entryName.formatNeo4jPropertyValue()}, " +
                " protein_names: ${uniprotModel.proteinNames.parseToNeo4jStringList()}, " +
                " gene_names: ${uniprotModel.geneNames.parseToNeo4jStringList(';')}, " +
                " protein_length: ${uniprotModel.length}, " +
                " created: datetime()}, " +
                " { last_mod: datetime()}) YIELD node AS ${UniprotModel.nodename} \n"

    companion object: CoreModelDao {
        /*
    Complete the relationships for this Hgnc node
    UniProt - [HAS_PUBLICATION] -> Publication

     */
        private fun completeRelationships(model: CoreModel): Unit{
            createPubMedRelationships(model)
        }

        override val modelRelationshipFunctions: (CoreModel) -> Unit
                = ::completeRelationships

    }
}