:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/GenomicCoreData/Entrez/Gene/human_gene2go.tsv' AS line FIELDTERMINATOR '\t'
CALL {WITH line
MATCH (eg:EntrezGene) WHERE eg.gene_id = line.GeneID
MATCH (go:GoTerm {obo_id: line.GO_ID})
MERGE (eg)-[r1:HAS_GO_TERM{qualifier: line.Qualifier}]->(go)
} IN TRANSACTIONS OF 3000 ROWS;
// filter human (i.e. tax id 9606) from gene2go file from NCBI
// grep "^9606\t" gene2go > human_gene2go.tsv