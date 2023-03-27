CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Seat5TBExt/GenomicCoreData/Reactome/UniProt2ReactomeReactions_human.tsv",
{ header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT nodeRecord.ReactomeID IS NULL
MERGE (react:Reactome {reactome_id: nodeRecord.ReactomeID})
ON CREATE
SET react.reactome_url =nodeRecord.ReactomeURL,
  react.reactome_pathway= nodeRecord.ReactomePathway
WITH react, nodeRecord
MATCH (uni:UniProtEntry) WHERE uni.entry_id = nodeRecord.UniProtID
MERGE (uni)-[r:HAS_REACTOME]->(react);
',
{batchSize:1000, iterateList:true, parallel:true});

