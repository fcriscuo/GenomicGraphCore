
// Separate defining eionships from define nodes and properties so that they
// data files can be loaded in any order
//
// relationship to UniProt via mapa_geneid_4_uniprot_crossref.tsv
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/DisGeNet/mapa_geneid_4_uniprot_crossref.tsv", 
{ header:true,  sep:"TAB"}
)
YIELD nodeRecordNo, map AS nodeRecord RETURN nodeRecord, nodeRecordNo
','
MATCH (gene:DisgenetGene) WHERE ltrim(gene.ncbi_gene_id) = ltrim(nodeRecord.GENEID)
MATCH (uni:UniProtEntry) WHERE uni.entry_id = nodeRecord.UniProtKB
MERGE (gene)-[r1:HAS_UNIPROT_ENTRY]->(uni)
',
{batchSize:4000, iterateList:true, parallel:true});

// DisGenetGene -> HGNC relationship
MATCH (gene:DisgenetGene) WHERE gene.gene_symbol IS NOT NULL
MATCH(hgnc:Hgnc) WHERE hgnc.gene_symbol = gene.gene_symbol
MERGE (gene) - [r:HAS_HGNC] -> (hgnc)
;

// DisGenetGene -> DisgenetVariant relationship
CALL apoc.periodic.iterate('
CALL apoc.load.csv("Volumes/Sea5TBExt/GenomicCoreData/DisGeNet/dbsnp_variant_to_gene_mappings.tsv",
{ header:true,  sep:"TAB"}
)
YIELD nodeRecordNo, map AS nodeRecord RETURN nodeRecord, nodeRecordNo
','
MATCH(gene:DisgenetGene) WHERE gene.gene_symbol = nodeRecord.geneSymbol
MATCH(var:DisgenetVariant) WHERE var.snp_id = nodeRecord.snpId
MERGE (gene)-[r1:HAS_DISGENET_VARIANT{source:nodeRecord.sourceId}]->(var)
',
{batchSize:4000, iterateList:true, parallel:true});

// DisGenetDisease -> HPO relationship
MATCH (dis:DisgenetDisease) where dis.hpo_class_id IS NOT NULL
UNWIND dis.hpo_class_id AS hpo_id
MATCH (hpo:HpTerm) WHERE hpo.obo_id = hpo_id
MERGE (dis)-[r:HAS_HPO_TERM]->(hpo)

// DisGenetDisease -> EntrezGene relationship
// edit downloaded file to split disease id & disease name
// add header: GeneId DiseaseId DiseaseName
CALL apoc.periodic.iterate('
CALL apoc.load.csv("Volumes/Sea5TBExt/GenomicCoreData/DAVID/ENTREZ_GENE/ENTREZ_GENE_ID2DISGENET.txt",
{ header:true,  sep:"TAB"}
)
YIELD nodeRecordNo, map AS nodeRecord RETURN nodeRecord, nodeRecordNo
','
MATCH (e:EntrezGene) where e.gene_id = nodeRecord.GeneID
MATCH (d:DisgenetDisease) WHERE d.disease_id = nodeRecord.DiseaseID
MERGE (e)-[r:HAS_DISGENET_DISEASE]->(d)
',
{batchSize:4000, iterateList:true, parallel:true});

// EntrezGene -> HGNC relationship
MATCH (eg:EntrezGene) WHERE eg.gene_symbol IS NOT NULL
MATCH(hgnc:Hgnc{gene_symbol:eg.gene_symbol})
MERGE (eg)-[r:HAS_HGNC]->(hgnc)
;

// EntrezGene -> GO term relationship
CALL apoc.periodic.iterate('
CALL apoc.load.csv("Volumes/Sea5TBExt/GenomicCoreData/Entrez/Gene/human_gene2go.tsv",
{ header:true,  sep:"TAB"}
)
YIELD nodeRecordNo, map AS nodeRecord RETURN nodeRecord, nodeRecordNo
','
MATCH (eg:EntrezGene) WHERE eg.gene_id = nodeRecord.GeneID
MATCH (go:GoTerm {obo_id: nodeRecord.GO_ID})
MERGE (eg)-[r1:HAS_GO_TERM{qualifier: nodeRecord.Qualifier}]->(go)
',
{batchSize:4000, iterateList:true, parallel:true});
// filter human (i.e. tax id 9606) from gene2go file from NCBI
// grep "^9606\t" gene2go > human_gene2go.tsv


// NHGRI_GENE -> HGNC
MATCH (gene:NHGRI_Gene) WHERE gene.hgnc_id IS NOT NULL
MATCH (hgnc:Hgnc) WHERE hgnc.hgnc_id = gene.hgnc_id
MERGE (gene)-[r1:HAS_HGNC]->(hgnc)

// UniProtEntry -> HGNC
MATCH (uni:UniProtEntry) WHERE uni.hgnc_id <> ' '
MATCH(hgnc:Hgnc{hgnc_id:uni.hgnc_id})
MERGE (uni) - [r:HAS_HGNC] -> (hgnc)
;

// UniProtEntry -> Gene Ontology relationship
MATCH (uni:UniProtEntry) WHERE uni.gene_ontology_ids <> ' '
UNWIND uni.gene_ontology_ids AS go_id
MATCH(obo: OboTerm) WHERE obo.obo_id = go_id
MERGE (uni) -[r:HAS_GENE_ONTOLOGY]-> (obo)
;

// UniProtEntry -> UniProtEntry relationship (self-interaction)
MATCH (uni:UniProtEntry) WHERE uni.interacts_with IS NOT NULL
UNWIND uni.interacts_with AS target_id
MATCH (target:UniProtEntry) WHERE  target.entry_id = target_id
MERGE (uni)-[r:INTERACTS_WITH]->(target)
;