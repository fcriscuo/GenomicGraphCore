
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
YIELD nodeRecordNo, map AS nodeRecord RETURN nodeRecord, nodeRecordNo
','
WHERE NOT nodeRecord.GENE IS NULL
MERGE (gene:NHGRI_Gene:Gene{gene_symbol: nodeRecord.GENE})
SET gene.hgnc_id = apoc.text.join(["HGNC",nodeRecord.`HGNC ID`],":"),
  gene.nhgri_gene_url = genomiccore.resolveNhgriGeneUrl(nodeRecord.GENE),
  gene.entrez_id = nodeRecord.`ENTREZ GENE ID`,
  gene.entrez_gene_url= genomiccore.resolveEntezUrl(gene.entrez_id),
  gene.conditions = nodeRecord.CONDITION,
  gene.inheritance = nodeRecord.INHERITANCE,
  gene.age_group = nodeRecord.`AGE GROUP`,
  gene.allelic_conditions = nodeRecord.`ALLELIC CONDITIONS`,
  gene.manifestation_categories = nodeRecord.`MANIFESTATION CATEGORIES`,
  gene.intervention_categories = nodeRecord.`INTERVENTION CATEGORIES`),
  gene.comments = nodeRecord.COMMENTS,
  gene.intervention_rationales = nodeRecord.`INTERVENTION/RATIONALE`,
  gene.pub_ids = nodeRecord.REFERENCES,)
',
{batchSize:4000, iterateList:true, parallel:true});

