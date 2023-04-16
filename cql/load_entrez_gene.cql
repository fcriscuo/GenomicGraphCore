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

// Supplement GenomicEntity -> Publication relationships
// Create a new Publication/PubMed node if novel
// to filter human (i.e. tax id 9606) from gene2pub file downloaded from NCBI
// grep "^9606\t" gene2pubmed > human_gene2pubmed.tsv
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/Entrez/Gene/human_gene2pubmed.tsv", { header:true,sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT nodeRecord.`PubMed_ID` IS NULL
MATCH (eg:EntrezGene) WHERE eg.gene_id = nodeRecord.GeneID
MERGE (p:Publication {pub_id: toInteger(nodeRecord.PubMed_ID)})
  ON CREATE
  SET p.url= genomiccore.resolvePubmedUrl(toString(p.pub_id)),
  p.needs_properties=true, p.needs_references=true
SET p:PubMed
MATCH (ge:GenomicEntity) WHERE ge.gene_symbol = eg.gene_symbol
MERGE (ge)-[r:HAS_PUBLICATION]->(p)
',
{batchSize:4000, iterateList:true});

