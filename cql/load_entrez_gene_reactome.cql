//Reactome
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/Reactome/NCBI2ReactomeReactions_human.tsv",
{ header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT nodeRecord.ReactomeID IS NULL
MERGE (react:Reactome {reactome_id: nodeRecord.ReactomeID})
  ON CREATE
  SET  react.reactome_url =nodeRecord.ReactomeURL,
  react.reactome_pathway= nodeRecord.ReactomePathway
WITH react, nodeRecord
MATCH (eg:EntrezGene) WHERE eg.gene_id = nodeRecord.EntrezID
MERGE (eg)-[r:HAS_REACTOME]-(react)
',
{batchSize:1000, iterateList:true});

