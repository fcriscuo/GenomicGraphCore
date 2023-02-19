:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/GenomicCoreData/Entrez/Gene/human_gene2pubmed.tsv' AS line FIELDTERMINATOR '\t'
CALL {WITH line
MATCH (eg:EntrezGene) WHERE eg.gene_id = line.GeneID
MERGE (p:Publication {pub_id: toInteger(line.PubMed_ID)})
ON CREATE
SET p.url= genomiccore.resolvePubmedUrl(toString(p.pub_id)),
p.needs_properties=true, p.needs_references=true
SET p:PubMed
MERGE (eg)-[r:HAS_PUBLICATION]->(p)
} IN TRANSACTIONS OF 3000 ROWS;
// filter human (i.e. tax id 9606) from gene2pub file from NCBI
// grep "^9606\t" gene2pubmed > human_gene2pubmed.tsv