:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/GenomicCoreData/Entrez/Gene/gene_info_9606.tsv' AS line FIELDTERMINATOR '\t'
CALL {WITH line
MERGE (eg:EntrezGene:Gene{gene_id: line.GeneID})

SET eg.ncbi_gene_url = genomiccore.resolveNcbiGeneUrl(ltrim(line.GeneID)),
  eg.gene_symbol = line.Symbol,
  eg.locus_tag = line.LocusTag,
  eg.synonyms = coalesce(split(line.Synonyms,'|'),' '),
  eg.db_xrefs = coalesce(split(line.dbXrefs,'|'),' '),
  eg.chromosome = line.chromosome,
  eg.map_location = line.map_location,
  eg.description = line.description,
  eg.type_of_gene = line.type_of_gene,
  eg.hgnc_symbol = line.Symbol_from_nomenclature_authority,
  eg.hgnc_gene_name = line.Full_name_from_nomenclature_authority,
  eg.nomenclature_status = line.Nomenclature_status,
  eg.other_designations = coalesce(split(line.Other_designations,'|'),' '),
  eg.created=datetime()
} IN TRANSACTIONS OF 3000 ROWS;

// set the gene_symbol property for a Entrez Gene entry based on its
// related HGNC symbol
//MATCH(hgnc:Hgnc{hgnc_id:uni.hgnc_id})
MATCH (eg:EntrezGene) WHERE eg.gene_symbol <> ' '
MATCH(hgnc:Hgnc{gene_symbol:eg.gene_symbol})
MERGE (eg)-[r:HAS_HGNC]->(hgnc)
;

