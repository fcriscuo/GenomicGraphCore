package org.batteryparkdev.genomicgraphcore.go

fun main() {
    val synonyms = listOf<String>(
        "synonym: \"activation of receptor internalization\" NARROW []",
        "synonym: \"stimulation of receptor internalization\" NARROW []",
        "synonym: \"up regulation of receptor internalization\" EXACT []",
        "synonym: \"up-regulation of receptor internalization\" EXACT []",
        "synonym: \"upregulation of receptor internalization\" EXACT []"
    )
    GoSynonym.resolveSynonyms(synonyms).forEach { syn ->
        println("Synonym: ${syn.synonymText}   ${syn.synonymType}")
    }
    val relationships = listOf<String>(
        "is_a: GO:0006355 ! regulation of transcription, DNA-templated",
        "is_a: GO:1903047 ! mitotic cell cycle process",
        "intersection_of: GO:0006355 ! regulation of transcription, DNA-templated",
        "intersection_of: part_of GO:0000082 ! G1/S transition of mitotic cell cycle",
        "relationship: part_of GO:0000082 ! G1/S transition of mitotic cell cycle"
    )
    Relationship.resolveRelationships(relationships).forEach { rel ->
        println("Relationship: ${rel.type}   ${rel.qualifier}  ${rel.targetId}   ${rel.description}")
    }

    val xrefs = listOf<String>(
        "xref: EC:3.2.1.108",
        "xref: MetaCyc:LACTASE-RXN",
        "xref: Reactome:R-HSA-189062 \"lactose + H2O => D-glucose + D-galactose\"",
        "xref: Reactome:R-HSA-5658001 \"Defective LCT does not hydrolyze Lac\"",
        "xref: RHEA:10076"
    )
    Xref.resolveXrefs(xrefs).forEach { xref ->
        println("XREF: ${xref.source}   ${xref.id}  ${xref.description}")
    }
    //PMIDs
    val PMIDlist = listOf ( "def: \"An enzyme complex that catalyzes the transfer of GlcNAc from UDP-GlcNA.\" [GOC:kp, GOC:rb, PMID:10944123, PMID:15163411]\n")
    GoTerm.resolvePubMedIdentifiers("GO:0000700",PMIDlist).forEach { rd ->
        println("RelDef: ${rd.parentNode}  ${rd.relationshipType}   ${rd.childNode}")
    }

}