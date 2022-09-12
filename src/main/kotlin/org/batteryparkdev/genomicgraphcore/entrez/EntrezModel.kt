package org.batteryparkdev.genomicgraphcore.entrez

import org.neo4j.driver.Value

data class EntrezModel (val entrezId: Int ){

    companion object{
        val nodename = "entrez"

        private fun generatePlaceholderCypher(entrezId: Int): String =
            "CALL apoc.merge.node([\"Entrez\"], " +
                    "{entrez_id: $entrezId,  created: datetime()} )" +
                    "YIELD node AS ${EntrezModel.nodename} \n "

        fun generateHasEntrezRelationship(entrezId: Int, parentNodeName: String): String {
            val relationship = "HAS_ENTREZ"
            val relName = "rel_entrez"
            return generatePlaceholderCypher(entrezId)
                .plus(
                    " CALL apoc.merge.relationship ($parentNodeName, '$relationship' ," +
                            " {}, {created: datetime()}," +
                            " ${EntrezModel.nodename}, {}) YIELD rel AS $relName \n"
                )
        }
    }
}