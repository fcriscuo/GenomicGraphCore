// UniProtEntry PubMed placeholders
MATCH (uni:UniProtEntry) WHERE uni.pubmed_ids <> ' '
UNWIND uni.pubmed_ids AS pubmed_id
MERGE (p:Publication:PubMed{pub_id:toInteger(ltrim(pubmed_id))})
ON CREATE
  SET p.url= genomiccore.resolvePubmedUrl(toString(p.pub_id)),
  p.needs_properties=true, p.needs_references=true
MERGE (uni) - [r:HAS_PUBLICATION] -> (p)
;

// EntrezGene Pubmed placeholders
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
MERGE (eg)-[r:HAS_PUBLICATION]->(p)
',
{batchSize:4000, iterateList:true, parallel:true});
// to filter human (i.e. tax id 9606) from gene2pub file from NCBI
// grep "^9606\t" gene2pubmed > human_gene2pubmed.tsv

// create a Publication/PubMed placeholder node if pub_id is novel
match (h:Hgnc) where h.pubmed_ids <> " "
unwind h.pubmed_ids AS pubmed_id
merge (p:Publication:PubMed{pub_id:toInteger(pubmed_id)})
ON CREATE
  SET p.url= genomiccore.resolvePubmedUrl(toString(p.pub_id)),
  p.needs_properties=true, p.needs_references=true
merge (h) - [r:HAS_PUBLICATION] -> (p)
;

//Create Publication placeholder nodes if pub_id is novel
match (gene:NHGRI_Gene) where gene.pub_ids IS NOT NULL
unwind gene.pub_ids AS pubmed_id
merge (p:Publication:PubMed{pub_id:toInteger(pubmed_id)})
ON CREATE
  SET p.url= genomiccore.resolvePubmedUrl(toString(p.pub_id)),
  p.needs_properties=true, p.needs_references=true
merge (gene) - [r2:HAS_PUBLICATION] -> (p)
;