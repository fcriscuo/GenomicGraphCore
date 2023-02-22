:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/GenomicCoreData/Reactome/NCBI2ReactomeReactions_human.tsv' AS line FIELDTERMINATOR '\t'
CALL {
WITH line
MERGE (react:Reactome {reactome_id: line.ReactomeID})
  ON CREATE
  SET  react.reactome_url =line.ReactomeURL,
  react.reactome_pathway= line.ReactomePathway
WITH react, line
MATCH (eg:EntrezGene) WHERE eg.gene_id = line.EntrezID
MERGE (eg)-[r:HAS_REACTOME]-(react)

} IN TRANSACTIONS OF 1000 ROWS;

