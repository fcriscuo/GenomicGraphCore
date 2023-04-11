
// CQL script to define relationships between independent nodes
// These relationships cannot be established until data for the independent
// loads have been loaded into the database. (e.g. GenomicEntity, HGNC, UniProt)
// By separating node creation from relationship definition, the relevant nodes can
// be loaded in any order.
//
//


// GenomicEntity -> HGNC
MATCH(hgnc:Hgnc) WHERE hgnc.gene_symbol IS NOT NULL
MATCH (ge:GenomicEntity) WHERE ge.gene_symbol = hgnc.gene_symbol
MERGE (ge)-[r:HAS_HGNC_ENTRY]->(hgnc)
;

//  GenomicEntity -> DisGenetGene relationship
MATCH (gene:DisgenetGene) WHERE gene.gene_symbol IS NOT NULL
MATCH(ge:GenomicEntity) WHERE ge.gene_symbol = gene.gene_symbol
MERGE (ge) - [r:HAS_DISGENET_GENE] -> (gene)
;


// DisGenetGene -> DisGenetDisease relationship (via EntrezGene)
// edit downloaded file to split disease id & disease name:
//    cd /Volumes/Sea5TBExt/GenomicCoreData/DAVID/EntrezGene
//    sed 's/~/\'$'\t/g' ENTREZ_GENE_ID2DISGENET.txt >  ENTREZ_GENE_ID2DISGENET.tsv
//    add header: GeneId<TAB>DiseaseId<TAB>>DiseaseName
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/DAVID/EntrezGene/ENTREZ_GENE_ID2DISGENET.tsv",
{ header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
MATCH (e:EntrezGene) where e.gene_id = nodeRecord.GeneId
MATCH (dg:DisgenetGene) where dg.ncbi_gene_id = e.gene_id
MATCH (d:DisgenetDisease) WHERE d.disease_id = nodeRecord.DiseaseId
MERGE (dg)-[r1:HAS_DISGENET_DISEASE]->(d)
',
{batchSize:4000, iterateList:true, parallel:true});

// GenomicEntity -> EntrezGene
MATCH (eg:EntrezGene) WHERE eg.gene_symbol IS NOT NULL
MATCH(ge:GenomicEntity) WHERE ge.gene_symbol = eg.gene_symbol
MERGE (ge)-[r:HAS_ENTREZ_GENE]->(eg)
;

// GenomicEntity -> NHGRI GENE
MATCH (gene:NHGRI_Gene) WHERE gene.gene_symbol IS NOT NULL
MATCH (ge:GenomicEntity) WHERE ge.gene_symbol = gene.gene_symbol
MERGE (ge)-[r1:HAS_NHGRI_GENE]->(hgene)
;


// GenomicEntity -> UniProtEntry relationship
MATCH (uni:UniProtEntry) WHERE uni.hgnc_ids IS NOT NULL
MATCH(hgnc:Hgnc) WHERE hgnc.hgnc_id = uni.hgnc_ids[0]
MATCH (ge:GenomicEntity) WHERE ge.gene_symbol = hgnc.gene_symbol
WITH ge, uni
MERGE (ge) - [r:HAS_UNIPROT_ENTRY] -> (uni)
;


