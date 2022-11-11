package org.batteryparkdev.genomicgraphcore.uniprot.dao

import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelDao
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.common.parseToNeo4jStringList
import org.batteryparkdev.genomicgraphcore.hgnc.HgncDao
import org.batteryparkdev.genomicgraphcore.uniprot.model.UniprotIdModel

class UniprotIdModelDao(private val uniprotIdModel: UniprotIdModel) {
     private val url = "https://www.uniprot.org/uniprotkb/" +
             "${uniprotIdModel.entryId}/entry"

    fun generateUniprotIdCypher(): String = generateMergeCypher()
        .plus(" RETURN  ${UniprotIdModel.nodename} \n")

    private fun generateMergeCypher(): String =
        " CALL apoc.merge.node(['UniProtEntry'], {entry_id: ${uniprotIdModel.entryId.formatNeo4jPropertyValue()}}, " +
                " { entry_name: ${uniprotIdModel.entryName.formatNeo4jPropertyValue()}," +
                " entry_url: ${url.formatNeo4jPropertyValue()}, " +
                " hgnc: ${uniprotIdModel.hgnc.formatNeo4jPropertyValue()}," +
                " ncbi_gene_id: ${uniprotIdModel.geneId}, " +
                " go_list: ${uniprotIdModel.goList.parseToNeo4jStringList(';')} ," +
                " created: datetime()}, " +
                " { last_mod: datetime()}) YIELD node AS ${UniprotIdModel.nodename} \n"

    companion object : CoreModelDao {

        private fun createHgncRelationship(model: CoreModel){
            if (model is UniprotIdModel && model.hgnc.isNotEmpty()){
                HgncDao.registerChildRelationshipToHgnc(model.hgnc, model)
            }
        }

        private fun completeRelationships( model: CoreModel): Unit{
            createHgncRelationship(model)
        }

        override val modelRelationshipFunctions: (CoreModel) -> Unit
                = ::completeRelationships

    }
}