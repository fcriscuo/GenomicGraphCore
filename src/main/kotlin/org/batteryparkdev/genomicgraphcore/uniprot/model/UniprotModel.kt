package org.batteryparkdev.genomicgraphcore.uniprot.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.genomicgraphcore.common.*
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.uniprot.dao.UniprotModelDao


data class UniprotModel(
    val entryId: String, val entryName: String, val proteinNames: String,
    val geneNames: String, val length: Int, val pubmedIds: List<Int>,
    val interactList: List<String>, val hgncId: String
) : CoreModel {

    override val idPropertyValue: String = this.entryId
    override fun getNodeIdentifier(): NodeIdentifier = generateNodeIdentifierByModel(UniprotModel,this)

    //override fun getNodeIdentifier(): NodeIdentifier = NodeIdentifier("UniProt", "entry_id", entryId)

    override fun generateLoadModelCypher(): String = UniprotModelDao(this).generateUniprotCypher()

    override fun createModelRelationships() = UniprotModelDao.modelRelationshipFunctions.invoke(this)

    override fun isValid(): Boolean = entryId.isNotEmpty().and(entryName.isNotEmpty())
        .and(proteinNames.isNotEmpty()).and(geneNames.isNotEmpty())

    override fun getPubMedIds(): List<Int> = pubmedIds

    override fun getModelGeneSymbol(): String =
        when (this.isValid()) {
            true -> geneNames.parseOnSemicolon()[0]
            false -> ""
        }

    override fun getModelSampleId(): String = ""

    companion object : CoreModelCreator {

        override val nodename = "uniprot"
        override val nodelabel: String
            get() = "UniProt"
        override val nodeIdProperty: String
            get() = "entry_id"

//        override fun generateNodeIdentifierByValue(idValue: String): NodeIdentifier =
//            NodeIdentifier("UniProt", "entry_id", idValue)

        private fun parseProteinNames( names: String): String =
            names.replace('(','|').replace(")","")

        private fun parseCsvRecord(record: CSVRecord): UniprotModel =
            UniprotModel(
                record.get("Entry"),
                record.get("Entry Name"),
                parseProteinNames( record.get("Protein names")),
                record.get("Gene Names"),
                record.get("Length").toInt(),
                record.get("PubMed ID").parseOnSemicolon().map { it.toInt() },
                record.get("Interacts with").parseOnSemicolon(),
                record.get("HGNC").replace(";","")
            )
        override val createCoreModelFunction: (CSVRecord) -> CoreModel = ::parseCsvRecord
    }
}