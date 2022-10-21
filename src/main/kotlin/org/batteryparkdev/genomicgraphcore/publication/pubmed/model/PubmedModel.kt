package org.batteryparkdev.genomicgraphcore.publication.pubmed.model

import ai.wisecube.pubmed.*
import arrow.core.invalid
import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelCreator
import org.batteryparkdev.genomicgraphcore.common.removeInternalQuotes
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.publication.pubmed.dao.PubmedDao
import org.batteryparkdev.genomicgraphcore.publication.pubmed.dao.ReferenceDao
import org.batteryparkdev.genomicgraphcore.publication.pubmed.service.PubmedRetrievalService


data class PubmedModel(
    val label: String,
    val pubmedId: Int,
    val parentPubMedId: Int,
    val pmcId: String = "",
    val doiId: String = "",
    val journalName: String,
    val journalIssue: String,
    val articleTitle: String,
    val abstract: String,
    val authors: String,
    val referenceList: List<PubmedReference>,
    val citationSet: Set<Int>,
    val citedByCount: Int
): CoreModel {

    override fun getNodeIdentifier(): NodeIdentifier = generateNodeIdentifierByModel(
        PubmedModel, this)

    override fun generateLoadModelCypher(): String = PubmedDao(this).generatePubMedCypher()

    override fun createModelRelationships() = PubmedDao.modelRelationshipFunctions.invoke(this)

    override val idPropertyValue: String
        get() = this.pubmedId.toString()

    override fun isValid(): Boolean = (this.pubmedId >0)
        .and(this.articleTitle.isNotEmpty())
        .and(this.journalName.isNotEmpty())

    // for PubmedModels, the PubMed IDs refer to its PubMed references, not its own id
    override fun getPubMedIds(): List<Int> = referenceList.stream().map { ref -> ref.referencePubmedId }.toList()

    override fun getModelGeneSymbol(): String  =""

    override fun getModelSampleId(): String = ""

    companion object: CoreModelCreator {
        /*
       Function to parse attributes from the PubMedArticle JaXB model object.
       Primary node label is Publication.
       Secondary label should be one of (PubMed, Reference, Citation)
        */
        fun parsePubMedArticle(pubmedArticle: PubmedArticle,
                               secondaryLabel: String = " ", parentId: Int = 0): PubmedModel {
            val pmid = pubmedArticle.medlineCitation.pmid.getvalue().toInt()
            val pmcid = resolveArticleIdByType(pubmedArticle, "pmc")
            val doiid = resolveArticleIdByType(pubmedArticle, "doi")
            val authors = generateAuthorCaption(pubmedArticle)
            val journalName = pubmedArticle.medlineCitation.article.journal.title
            val journalIssue = resolveJournalIssue(pubmedArticle)
            val title =  pubmedArticle.medlineCitation.article.articleTitle.getvalue().removeInternalQuotes()
            val abstract = resolveAbstract(pubmedArticle).removeInternalQuotes()
            val citations = PubmedRetrievalService.retrieveCitationIds(pmid.toString())

            return PubmedModel(
                secondaryLabel, pmid, parentId,
                pmcid, doiid, journalName, journalIssue, title,
                abstract, authors, resolveReferenceList(pubmedArticle,pmid),
                citations, citations.size
            )
        }

        /*
        Private function to collect the PubMedReferences
        references
         */
        private fun resolveReferenceList(pubmedArticle: PubmedArticle, parentId: Int): List<PubmedReference> {
            val refList = mutableListOf<PubmedReference>()
                pubmedArticle.pubmedData.referenceList.stream().forEach { refL ->
                    refL.reference.stream().forEach { ref ->
                        refList.add(PubmedReference.parsePubMedReference(ref,parentId))
                    }
                }
            return refList
        }

        /*
        Private function to resolve the article's abstract
         */
        private fun resolveAbstract(pubmedArticle: PubmedArticle): String {
            val absTextList = pubmedArticle.medlineCitation.article?.abstract?.abstractText ?: listOf<AbstractText>()
            return when (absTextList.isNotEmpty()) {
                true ->  absTextList[0].getvalue()
                false -> ""
            }
        }

        /*
        Private function to resolve an article's id based on a supplied type
         */
        private fun resolveArticleIdByType(pubmedArticle: PubmedArticle, type: String): String {
            val articleId = pubmedArticle.pubmedData.articleIdList.articleId.firstOrNull { it.idType == type }
            return when (articleId != null) {
                true ->articleId.getvalue()
                false -> ""
            }
        }
        /*
       Function to generate a String with the names of the first
       two (max) authors plus et al. if > 2 authors
       e.g.  Smith, Robert; Jones, Mary, et al
        */
        private fun generateAuthorCaption(pubmedArticle: PubmedArticle): String {
            val authorList = pubmedArticle.medlineCitation.article?.authorList?.author
            if (authorList != null) {
                val ret = when (authorList.size) {
                    0 -> ""
                    1 -> processAuthorName(authorList[0])
                    2 -> processAuthorName(authorList[0]) + "; " +
                            processAuthorName(authorList[1])
                    else -> processAuthorName(authorList[0]) + "; " +
                            processAuthorName(authorList[1]) + "; et al"
                }
                return ret
            }
            return ""
        }

        /*
        Private function to resolve author names
         */
        private fun processAuthorName(author: Author): String {
            val authorNameList = author.lastNameOrForeNameOrInitialsOrSuffixOrCollectiveName
            var name = ""
            if (authorNameList[0] is CollectiveName) {
                return (authorNameList[0] as CollectiveName).getvalue()
            }
            val lastName: LastName = authorNameList[0] as LastName
            name = lastName.getvalue()
            if (authorNameList.size > 1) {
                val name1  = when (authorNameList[1]) {
                    is ForeName -> (authorNameList[1] as ForeName).getvalue()
                    is Initials -> (authorNameList[1] as Initials).getvalue()
                    else -> (authorNameList[1] as Suffix).getvalue()
                }
                name = "$name, $name1"
            }
            return name
        }

        private fun processPagination(page: Pagination): String {
            val medlinePgn = page.startPageOrEndPageOrMedlinePgn[0] as MedlinePgn
            return medlinePgn.getvalue()
        }

        private fun processELocation(eloc: ELocationID): String = eloc.getvalue()

        private fun resolveJournalIssue(pubmedArticle: PubmedArticle): String {
            var ret = ""
            val journalIssue = pubmedArticle.medlineCitation.article.journal.journalIssue
            val year = if (journalIssue.pubDate.yearOrMonthOrDayOrSeasonOrMedlineDate[0] is Year) {
                (journalIssue.pubDate.yearOrMonthOrDayOrSeasonOrMedlineDate[0] as Year).getvalue()
            } else ""
            val vol = journalIssue.volume ?: ""
            val issue = journalIssue.issue ?: ""
            var pgn: String = ""
            if (pubmedArticle.medlineCitation.article.paginationOrELocationID.size > 0) {
                pgn = when (pubmedArticle.medlineCitation.article.paginationOrELocationID[0]) {
                    is Pagination -> processPagination(
                        pubmedArticle.medlineCitation.article.paginationOrELocationID[0]
                                as Pagination
                    )
                    is ELocationID -> processELocation(
                        pubmedArticle.medlineCitation.article.paginationOrELocationID[0]
                                as ELocationID
                    )
                    else -> ""
                }
            }
            if (vol.isNotEmpty()) {
                ret = "$year $vol"
                if (issue.isNotEmpty()) {
                    ret = "$ret($issue)"
                    if (pgn.isNotEmpty()) {
                        ret = "$ret:${pgn}"
                    }
                }
            }
            return ret
        }

        override val createCoreModelFunction: (CSVRecord) -> CoreModel
            get() = TODO("Not yet implemented")

        override val nodename = "publication"
        override val nodelabel: String
            get() = "Publication"
        override val nodeIdProperty: String
            get() = "pub_id"
    }
}

data class PubmedReference(val referencePubmedId: Int, val journal: String,
                           val date: String, val issue: String, val parentId: Int): CoreModel {

    companion object: CoreModelCreator {
        const val PubmedIdType = "pubmed"

        fun resolvePubMedReferenceId(idList: ArticleIdList):Int {
            val refId = idList.articleId.firstOrNull() { it.idType == PubmedIdType }
            return  when (refId != null) {
                true -> refId.getvalue().toInt()
                false -> 0
            }
        }

        private fun resolveJournalFromCitation(citation: String) =
                when (citation.contains('.')) {
                    true -> citation.substring(0, citation.indexOf('.'))
                    false -> ""
                }

        private fun resolveDateFromCitation(citation: String) =
            when(citation.contains('.').and(citation.contains(';'))){
                true -> citation.substring(citation.indexOf('.')+1, citation.indexOf(';')).trim()
                false ->""
            }

        private fun resolveIssueFromCitation(citation: String) =
            when (citation.contains(';')) {
                true -> citation.substring(citation.indexOf(';')+1)
                false -> ""
            }

        fun  parsePubMedReference(reference: Reference,parentId: Int) =
            PubmedReference(
                if (reference.articleIdList != null ) resolvePubMedReferenceId(reference.articleIdList) else 0,
                resolveJournalFromCitation( reference.citation),
                resolveDateFromCitation(reference.citation),
                resolveIssueFromCitation(reference.citation),
                parentId  // allows each Reference node to be persisted independently of the PubMed node
            )

        override val createCoreModelFunction: (CSVRecord) -> CoreModel
            get() = TODO("Not yet implemented")

        override val nodename = "reference"
        override val nodelabel: String
            get() = "Publication"
         override val nodeIdProperty: String
            get() = "pub_id"
        val secondaryLabel = "Reference"
    }

    override fun getNodeIdentifier(): NodeIdentifier = NodeIdentifier(PubmedReference.nodelabel,
    PubmedReference.nodeIdProperty, idPropertyValue, PubmedReference.secondaryLabel)

    override fun generateLoadModelCypher(): String =
       ReferenceDao(this).generateReferenceCypher()

    override fun createModelRelationships() = ReferenceDao.modelRelationshipFunctions.invoke(this)

    override val idPropertyValue: String
        get() = referencePubmedId.toString()

    override fun isValid(): Boolean  = (referencePubmedId> 0)
        .and(journal.isNotEmpty())
        .and(issue.isNotEmpty())

    override fun getPubMedIds(): List<Int> = listOf(referencePubmedId)

    override fun getModelGeneSymbol(): String = ""

    override fun getModelSampleId(): String  = ""

}

