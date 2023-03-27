//DAVID Gene Symbol

CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5tBExt/GenomicCoreData/DAVID/GeneSymbol/OFFICIAL_GENE_SYMBOL2DAVID_GENE_NAME.tsv",
{ header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT nodeRecord.GeneSymbol IS NULL
MERGE (ge:GenomicEntity:Gene{gene_symbol:nodeRecord.GeneSymbol})
SET ge.gene_name = nodeRecord.GeneName,
 ge.genecards_url = replace("https://genecards.org/cgi-bin/carddisp?gene=XXXX","XXXX",nodeRecord.GeneSymbol)
',
{batchSize:4000, iterateList:true, parallel:true});