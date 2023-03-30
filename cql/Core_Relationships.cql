
// Separate script for defining relationships from defining nodes and properties so that the
// data files can be loaded in any order
//

// GenomicEntity -> GeneOntology Biological Process
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/DAVID/GeneSymbol/OFFICIAL_GENE_SYMBOL2GOTERM_BP_ALL.tsv",
{ header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
MATCH (ge: GenomicEntity) WHERE ge.gene_symbol = NodeRecord.`GeneSymbol`
MATCH (go:GoTerm) WHERE WHERE go.obo_id = NodeRecord.`GoTerm`
MERGE (ge)-[r:HAS_GO_TERM {type:"Biological Process"}]->(go)
',
{batchSize:4000, iterateList:true, parallel:true});

// GenomicEntity -> GeneOntology Molecular Function
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/DAVID/GeneSymbol/OFFICIAL_GENE_SYMBOL2GOTERM_MF_ALL.tsv",
{ header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
MATCH (ge: GenomicEntity) WHERE ge.gene_symbol = NodeRecord.`GeneSymbol`
MATCH (go:GoTerm) WHERE WHERE go.obo_id = NodeRecord.`GoTerm`
MERGE (ge)-[r:HAS_GO_TERM {type:"Molecular Function"}]->(go)
',
{batchSize:4000, iterateList:true, parallel:true});

// GenomicEntity -> GeneOntology Cellular Component
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/DAVID/GeneSymbol/OFFICIAL_GENE_SYMBOL2GOTERM_CC_ALL.tsv",
{ header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
MATCH (ge: GenomicEntity) WHERE ge.gene_symbol = NodeRecord.`GeneSymbol`
MATCH (go:GoTerm) WHERE WHERE go.obo_id = NodeRecord.`GoTerm`
MERGE (ge)-[r:HAS_GO_TERM {type:"Cellular Component"}]->(go)
',
{batchSize:4000, iterateList:true, parallel:true});


// relationship to UniProt via mapa_geneid_4_uniprot_crossref.tsv
// UniProt -> DisGeNetGene
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/DisGeNet/mapa_geneid_4_uniprot_crossref.tsv", 
{ header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
MATCH (gene:DisgenetGene) WHERE ltrim(gene.ncbi_gene_id) = ltrim(nodeRecord.GENEID)
MATCH (uni:UniProtEntry) WHERE uni.entry_id = nodeRecord.UniProtKB
MERGE (uni)-[r1:HAS_DISGENET_GENE]->(gene)
',
{batchSize:4000, iterateList:true, parallel:true});

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

// DisGenetGene -> DisGenetVariant relationship
CALL apoc.periodic.iterate('
CALL apoc.load.csv("Volumes/Sea5TBExt/GenomicCoreData/DisGeNet/dbsnp_variant_to_gene_mappings.tsv",
{ header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
MATCH(gene:DisgenetGene) WHERE gene.gene_symbol = nodeRecord.geneSymbol
MATCH(var:DisgenetVariant) WHERE var.snp_id = nodeRecord.snpId
MERGE (gene)-[r1:HAS_DISGENET_VARIANT{source:nodeRecord.sourceId}]->(var)
',
{batchSize:4000, iterateList:true, parallel:true});


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
MATCH (dg:DisgenetGene) where dg.gene_id = e.gene_id
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


// GenomicEntity -> UniProtEntry relationship
MATCH (uni:UniProtEntry) WHERE uni.hgnc_id <> ' '
MATCH(hgnc:Hgnc{hgnc_id:uni.hgnc_id})
MATCH (ge:GenomicEntity) WHERE ge.gene_symbol = hgnc.gene_symbol
MERGE (ge) - [r:HAS_UNIPROT_ENTRY] -> (uni)
;


// UniProtEntry -> UniProtEntry relationship (self-interaction)
MATCH (uni:UniProtEntry) WHERE uni.interacts_with IS NOT NULL
UNWIND uni.interacts_with AS target_id
MATCH (target:UniProtEntry) WHERE  target.entry_id = target_id
MERGE (uni)-[r:INTERACTS_WITH]->(target)
;