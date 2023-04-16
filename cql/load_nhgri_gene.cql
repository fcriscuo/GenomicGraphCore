
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/NHGRI/NCBI_Clinical_Genomic_Database.tsv",
{ header:true,  sep:"TAB",
  mapping:{
    `CONDITION`: {array:true, arraySep:";"},
    `ALLELIC CONDITIONS`: {array:true, arraySep:";"},
    `MANIFESTATION CATEGORIES`: {array:true, arraySep:";"},
    `INTERVENTION CATEGORIES`: {array:true, arraySep:";"},
    `INTERVENTION/RATIONALE`: {array:true, arraySep:";"},
    `REFERENCES`: {array:true, arraySep:";"}
  }
}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT nodeRecord.GENE IS NULL
MERGE (gene:NHGRI_Gene:Gene{gene_symbol: nodeRecord.GENE})
SET gene.hgnc_id = apoc.text.join(["HGNC",nodeRecord.`HGNC ID`],":"),
  gene.nhgri_gene_url = genomiccore.resolveNhgriGeneUrl(nodeRecord.GENE),
  gene.entrez_id = nodeRecord.`ENTREZ GENE ID`,
  gene.entrez_gene_url= replace("https://www.ncbi.nlm.nih.gov/gene/?term=XXXX","XXXX",gene.entrez_id),
  gene.conditions = nodeRecord.CONDITION,
  gene.inheritance = nodeRecord.INHERITANCE,
  gene.age_group = nodeRecord.`AGE GROUP`,
  gene.allelic_conditions = nodeRecord.`ALLELIC CONDITIONS`,
  gene.manifestation_categories = nodeRecord.`MANIFESTATION CATEGORIES`,
  gene.intervention_categories = nodeRecord.`INTERVENTION CATEGORIES`,
  gene.comments = nodeRecord.COMMENTS,
  gene.intervention_rationales = nodeRecord.`INTERVENTION/RATIONALE`,
  gene.pub_ids = nodeRecord.REFERENCES
',
{batchSize:4000, iterateList:true});

//Supplement GenomicEntity collection of Publication/PubMed relationships
//Create Publication placeholder nodesif pub_id is novel
MATCH (gene:NHGRI_Gene)
  WHERE gene.pub_ids IS NOT NULL AND NOT isEmpty(gene.pub_ids)
UNWIND gene.pub_ids AS pub_id
WITH pub_id, gene
  WHERE pub_id IS NOT NULL AND toInteger(ltrim(pub_id)) IS NOT NULL
MERGE (p:Publication:PubMed{pub_id:toInteger(ltrim(pub_id))})
  ON CREATE SET p.url= genomiccore.resolvePubmedUrl(toString(p.pub_id)), p.needs_properties=true, p.needs_references=true
MATCH (ge:GenomicEntity) WHERE ge.gene_symbol = gene.gene_symbol
MERGE (ge)-[r2:HAS_PUBLICATION]->(p)
;

