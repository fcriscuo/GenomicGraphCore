package org.batteryparkdev.genomicgraphcore.publication.pubmed.service

import ai.wisecube.pubmed.PubmedArticle
import ai.wisecube.pubmed.PubmedParser
import arrow.core.Either
import io.ktor.util.reflect.*
import kotlinx.coroutines.delay
import org.batteryparkdev.genomicgraphcore.common.service.LogService
import org.batteryparkdev.genomicgraphcore.common.service.log
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition
import org.batteryparkdev.genomicgraphcore.publication.pubmed.model.PubmedModel
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.StringReader
import java.net.URL
import java.nio.charset.Charset
import javax.xml.parsers.DocumentBuilderFactory

object PubmedRetrievalService {

    private val ncbiEmail = System.getenv("NCBI_EMAIL")
    private val ncbiApiKey = System.getenv("NCBI_API_KEY")
    private val dbFactory = DocumentBuilderFactory.newInstance()
    private val dBuilder = dbFactory.newDocumentBuilder()
    const val ncbiDelay: Long = 100L   // 333 milliseconds w/o registered account

    @OptIn(ExperimentalStdlibApi::class)
    private fun generateEutilsURLByType(type: String, pubmedId: String):String {
       val template = when(type.lowercase()) {
           "pubmed" -> "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&amp;id=PUBMEDID&amp;retmode=xml" +
                   "&tool=my_tool&email=NCBIEMAIL&api_key=APIKEY"
           "citation" -> "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?dbfrom=pubmed&linkname=pubmed_pubmed_citedin" +
                   "&id=PUBMEDID&&tool=my_tool&email=NCBIEMAIL&api_key=APIKEY"
           "reference" ->"https://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?dbfrom=pubmed&linkname=pubmed_pubmed_refs" +
                   "&id=PUBMEDID&&tool=my_tool&email=NCBIEMAIL&api_key=APIKEY"
           else -> "$type is an invalid URL type"
       }
       return template.replace("PUBMEDID", pubmedId)
            .replace("NCBIEMAIL", ncbiEmail)
            .replace("APIKEY", ncbiApiKey)
    }

    /*
  Return an Either<Exception, PubMedArticle to deal with NCBI
  service disruptions
   */
    fun retrievePubMedArticle(pubmedId: String): Either<Exception, PubmedArticle> {
        Thread.sleep(ncbiDelay)  // Accommodate NCBI maximum request rate
        val url = generateEutilsURLByType("pubmed", pubmedId)
        return try {
            val text = URL(url).readText(Charset.defaultCharset())
            val parser = PubmedParser()
            val articleSet = parser.parse(text, ai.wisecube.pubmed.PubmedArticleSet::class.java)
            Either.Right(articleSet.pubmedArticleOrPubmedBookArticle[0] as PubmedArticle)
        } catch (e: Exception) {
            Either.Left(e)
        }
    }
    /*
    NCBI PubMed batch fetch request
    Input - Set of PubMed Ids
    Output - Either<Exception, List<PubmedArticle>
     */
    fun retrievePubMedArticleBatch(pubmedIdSet: Set<String>): Either<Exception,List<PubmedArticle>> {
        val pubmedId = pubmedIdSet.joinToString(separator = ",")
        val url = generateEutilsURLByType("pubmed", pubmedId)
        val articleList = mutableListOf<PubmedArticle>()
        return try {
            val text = URL(url).readText(Charset.defaultCharset())
            val parser = PubmedParser()
            val articleSet = parser.parse(text, ai.wisecube.pubmed.PubmedArticleSet::class.java)
            // filter out PubmedBook objects
            articleSet.pubmedArticleOrPubmedBookArticle.stream().filter { it is ai.wisecube.pubmed.PubmedArticle}
                .forEach{art -> articleList.add(art as PubmedArticle)}
            Either.Right(articleList)
        } catch (e: Exception) {
            println(e.message)
            Either.Left(e)
        }
    }

    fun retrieveCitationIds(pubmedId: String): Set<Int> {
        val url = generateEutilsURLByType("citation", pubmedId.toString())
        val citationSet = mutableSetOf<Int>()
        try {
            val text = URL(url).readText(Charset.defaultCharset())
            val xmlDoc = dBuilder.parse(InputSource(StringReader(text)));
            xmlDoc.documentElement.normalize()
            val citationList: NodeList = xmlDoc.getElementsByTagName("Link")
            for (i in 0 until citationList.length) {
                val citationNode = citationList.item(i)
                if (citationNode.nodeType == Node.ELEMENT_NODE) {
                    val elem = citationNode as Element
                    val id = elem.getElementsByTagName("Id").item(0).textContent
                    citationSet.add(id.toInt())
                }
            }
        } catch (e: Exception) {
            LogService.error("++++  EXCEPTION getting citation set for $pubmedId")
            e.log()
        }
        return citationSet.toSet()
    }

    /*
    Function to generate a Set of PlaceholderNode objects representing
    the articles referenced by the specified PubMed article
    These PlaceholderNode objects are labeled "Reference"
     */
    fun generateReferencePlaceholderNodes(pubmedId: Int): Set<RelationshipDefinition> {
        val relationshipDefSet = mutableSetOf<RelationshipDefinition>()
        retrieveReferenceIds(pubmedId.toString()).forEach { ref ->
            run {
                val parentNode = NodeIdentifier("Publication", "pub_id", pubmedId.toString(),
                "PubMed")
                val childNode = NodeIdentifier(
                    "Publication", "pub_id", ref.toString(),
                    "Reference"
                )
                relationshipDefSet.add(RelationshipDefinition(parentNode, childNode, "HAS_REFERENCE"))
            }
        }
        return relationshipDefSet.toSet()
    }

    /*
    Function to retrieve the PubMed Ids of the articles referenced by
    the specified PubMed Id
     */
    fun retrieveReferenceIds(pubmedId: String): Set<Int> {
        val url = generateEutilsURLByType("reference", pubmedId.toString())
        val referenceSet = mutableSetOf<Int>()
        try {
            val text = URL(url).readText(Charset.defaultCharset())
            val xmlDoc = dBuilder.parse(InputSource(StringReader(text)));
            xmlDoc.documentElement.normalize()
            val referenceList: NodeList = xmlDoc.getElementsByTagName("Link")
            for (i in 0 until referenceList.length) {
                val referenceNode = referenceList.item(i)
                if (referenceNode.nodeType == Node.ELEMENT_NODE) {
                    val elem = referenceNode as Element
                    val id = elem.getElementsByTagName("Id").item(0).textContent
                    referenceSet.add(id.toInt())
                }
            }
        } catch (e: Exception) {
            LogService.error("++++  EXCEPTION getting reference set for $pubmedId")
            e.log()
        }
        LogService.fine("+++Reference set size = ${referenceSet.size}")
        return referenceSet.toSet()
    }
}
