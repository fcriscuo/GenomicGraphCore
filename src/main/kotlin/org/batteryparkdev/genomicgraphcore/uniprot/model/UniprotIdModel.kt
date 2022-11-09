package org.batteryparkdev.genomicgraphcore.uniprot.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelCreator
import org.batteryparkdev.genomicgraphcore.common.datamining.loadFileFromUrl
import org.batteryparkdev.genomicgraphcore.common.parseValidInteger
import org.batteryparkdev.genomicgraphcore.common.service.FilesPropertyService
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.uniprot.dao.UniprotIdModelDao

/*
Represents the properties needed to define a UniProt placeholder node and to
establish relationships to HGNC and Gene Ontology nodes
 */
data class UniprotIdModel(val entryId: String, val reviewed: Boolean, val entryName: String,
                          val hgnc: String,
                          val geneId: Int,
                          val goList: String
): CoreModel {
    init {
        require (entryId.isNotEmpty())
    }

    override fun getNodeIdentifier(): NodeIdentifier = generateNodeIdentifierByModel(UniprotIdModel, this)

    override fun generateLoadModelCypher(): String = UniprotIdModelDao(this).generateUniprotIdCypher()

    override fun createModelRelationships() = UniprotIdModelDao.modelRelationshipFunctions.invoke(this)

    override val idPropertyValue: String
        get() = entryId

    override fun isValid(): Boolean = entryId.isNotEmpty().and(reviewed)

    override fun getPubMedIds(): List<Int> = emptyList()

    override fun getModelGeneSymbol(): String  = hgnc

    override fun getModelSampleId(): String = ""

    companion object : CoreModelCreator {

        private fun parseCsvRecord(record: CSVRecord): UniprotIdModel =
            UniprotIdModel(
                record.get("Entry"),
               record.get("Reviewed")=="reviewed",
                record.get("Entry Name"),
                record.get("HGNC").replace(";",""),
                record.get("GeneID").replace(";","").parseValidInteger(),
                record.get("Gene Ontology IDs")
            )
        override val createCoreModelFunction: (CSVRecord) -> CoreModel = UniprotIdModel.Companion::parseCsvRecord
        override val nodename: String
            get() = "uniprot_entry"
        override val nodelabel: String
            get() = "UniProtEntry"
        override val nodeIdProperty: String
            get() = "entryId"

        // Function to retrieve the identifiers data from UniProt and store it in a local file
        // for processing
        fun retrieveRemoteDataFile(): String {
            val uniprotIdRestUrl = FilesPropertyService.uniprotIdentifiersRestUrl
            val uniprotIdFileName = FilesPropertyService.uniprotIdFilename
            loadFileFromUrl(uniprotIdFileName, uniprotIdRestUrl)
            return uniprotIdFileName
        }
    }
}