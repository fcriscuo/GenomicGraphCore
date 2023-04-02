CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/Entrez/Gene/Homo_sapiens_gene_info.tsv",
{ header:true,  sep:"TAB",
  mapping: {
  `Synonyms`: {array:true, arraySep:"|"},
  `dbXrefs`: {array:true, arraySep:"|"},
  `Other_designations`: {array:true, arraySep:"|"}
  }
 }
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT nodeRecord.GeneID IS NULL
MERGE (eg:EntrezGene:Gene{gene_id: nodeRecord.GeneID})
SET eg.ncbi_gene_url = genomiccore.resolveNcbiGeneUrl(ltrim(nodeRecord.GeneID)),
  eg.gene_symbol = nodeRecord.Symbol,
  eg.locus_tag = nodeRecord.LocusTag,
  eg.synonyms = nodeRecord.Synonyms,
  eg.db_xrefs = nodeRecord.dbXrefs,
  eg.chromosome = nodeRecord.chromosome,
  eg.map_location = nodeRecord.map_location,
  eg.description = nodeRecord.description,
  eg.type_of_gene = nodeRecord.type_of_gene,
  eg.hgnc_symbol = nodeRecord.Symbol_from_nomenclature_authority,
  eg.hgnc_gene_name = nodeRecord.Full_name_from_nomenclature_authority,
  eg.nomenclature_status = nodeRecord.Nomenclature_status,
  eg.other_designations = nodeRecord.Other_designations
',
{batchSize:4000, iterateList:true});


