package org.batteryparkdev.genomicgraphcore.common.service

object XrefUrlPropertyService {

    const val templateValue = "ID"
    private val geneontologyUrlTemplate = "https://reactome.org/content/detail/$templateValue"
    private val reactomeUrlTemplate:String = "https://reactome.org/content/detail/$templateValue"
    private val keggUrlTemplate: String = "https://www.genome.jp/dbget-bin/www_bget?ec:$templateValue"
    private  val keggReactionUrlRemplate = "https://www.genome.jp/entry/$templateValue"
    private  val metacycUrlTemplate = "https://metacyc.org/META/new-image?object=$templateValue"
    private  val rheaUrlTemplate = "https://www.rhea-db.org/rhea/$templateValue"
    private  val wikipediaUrlTemplate = "https://en.wikipedia.org/wiki/$templateValue"

    fun resolveXrefUrl(source: String, id: String): String =
        when (source) {
            "Reactome" ->  reactomeUrlTemplate.replace(templateValue,id)
            "GeneOntology" -> geneontologyUrlTemplate.replace(templateValue,id)
            "EC" -> keggUrlTemplate.replace(templateValue,id)
            "KEGG_REACTION" -> keggReactionUrlRemplate.replace(templateValue,id)
            "MetaCyc" -> metacycUrlTemplate.replace(templateValue,id)
            "RHEA" -> rheaUrlTemplate.replace(templateValue,id)
            "Wikipedia" -> wikipediaUrlTemplate.replace(templateValue,id)
            else ->""
        }
}
