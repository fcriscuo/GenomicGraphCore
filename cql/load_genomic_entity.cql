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
{batchSize:4000, iterateList:true});

// GenomicEntity -> GeneOntology Biological Process
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/DAVID/GeneSymbol/OFFICIAL_GENE_SYMBOL2GOTERM_BP_ALL.tsv",
{ header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
MATCH (ge: GenomicEntity) WHERE ge.gene_symbol = nodeRecord.`GeneSymbol`
MATCH (go:GoTerm) WHERE go.obo_id = nodeRecord.`GoTerm`
MERGE (ge)-[r:HAS_GO_TERM {type:"Biological Process"}]->(go)
',
{batchSize:4000, iterateList:true});

// GenomicEntity -> GeneOntology Molecular Function
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/DAVID/GeneSymbol/OFFICIAL_GENE_SYMBOL2GOTERM_MF_ALL.tsv",
{ header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
MATCH (ge: GenomicEntity) WHERE ge.gene_symbol = nodeRecord.`GeneSymbol`
MATCH (go:GoTerm) WHERE go.obo_id = nodeRecord.`GoTerm`
MERGE (ge)-[r:HAS_GO_TERM {type:"Molecular Function"}]->(go)
',
{batchSize:4000, iterateList:true});

// GenomicEntity -> GeneOntology Cellular Component
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/DAVID/GeneSymbol/OFFICIAL_GENE_SYMBOL2GOTERM_CC_ALL.tsv",
{ header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
MATCH (ge: GenomicEntity) WHERE ge.gene_symbol = nodeRecord.`GeneSymbol`
MATCH (go:GoTerm) WHERE go.obo_id = nodeRecord.`GoTerm`
MERGE (ge)-[r:HAS_GO_TERM {type:"Cellular Component"}]->(go)
',
{batchSize:4000, iterateList:true});

// Add the chromosome property to Genomic Entity
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/DAVID/GeneSymbol/OFFICIAL_GENE_SYMBOL2CHROMOSOME.tsv",
{ header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
MATCH (ge:GenomicEntity) where ge.gene_symbol = nodeRecord.`GeneSymbol`
SET ge.chromosome = nodeRecord.`Chromosome`
',
{batchSize:4000, iterateList:true});

// Add the cytogenetic location property to Genomic Entity
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/DAVID/GeneSymbol/OFFICIAL_GENE_SYMBOL2CYTOGENETIC_LOCATION.tsv",
{ header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
MATCH (ge:GenomicEntity) where ge.gene_symbol = nodeRecord.`GeneSymbol`
SET ge.cytogenetic_location = nodeRecord.`CytogeneticLocation`
',
{batchSize:4000, iterateList:true});

// Supplement GenomicEntity properties with GENCODE gene data
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/GENCODE/gencode_gene.tsv",
{ header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
MATCH (ge:GenomicEntity) where ge.gene_symbol = nodeRecord.`GeneSymbol`
SET ge.gene_type = nodeRecord.`GeneType`,
ge.ensembl_gene_id = nodeRecord.`GeneId`,
ge.start = toInteger(nodeRecord.`Start`),
ge.end = toInteger(nodeRecord.`End`),
ge.strand = nodeRecord.`Strand`
',
{batchSize:4000, iterateList:true});

// Add Entrez Gene Summary property to GenomicEntity nodes
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/DAVID/GeneSymbol/OFFICIAL_GENE_SYMBOL2ENTREZ_GENE_SUMMARY.tsv",
{ header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
MATCH (ge:GenomicEntity) where ge.gene_symbol = nodeRecord.`GeneSymbol`
SET ge.entrez_gene_summary = nodeRecord.`GeneSummary`
',
{batchSize:4000, iterateList:true});

// Create gene to gene interaction relationships using BioGRID data
// https://thebiogrid.org/
// Stark C, Breitkreutz BJ, Reguly T, Boucher L, Breitkreutz A, Tyers M. Biogrid: A General Repository for Interaction Datasets. Nucleic Acids Res. Jan 1, 2006; 34:D535-9.
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/BioGRID/BIOGRID-ORGANISM-Homo_sapiens-4.4.220.tab3.tsv",
{ header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
MATCH (ge1:GenomicEntity) WHERE ge1.gene_symbol = nodeRecord.`Official Symbol Interactor A`
MATCH (ge2:GenomicEntity) WHERE ge2.gene_symbol = nodeRecord.`Official Symbol Interactor B`
WITH ge1, ge2, nodeRecord
WHERE NOT (ge2)-[:INTERACTS_WITH]-(ge1)
MERGE (ge1)-[r:INTERACTS_WITH]-(ge2)
WITH r, nodeRecord
WHERE nodeRecord.`Publication Source` IS NOT NULL
SET r.pub_id = toInteger(replace(nodeRecord.`Publication Source`,"PUBMED:",""))
',
{batchSize:4000, iterateList:true});

// GenomicEntity -> Publication/PubMed relationship
// Create a Publication placeholder node if Publication is novel
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/DAVID/GeneSymbol/OFFICIAL_GENE_SYMBOL2PUBMED_ID.tsv", { header:true,sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT nodeRecord.`PMID` IS NULL
MATCH (ge:GenomicEntity) WHERE ge.gene_symbol = nodeRecord.`GeneSymbol`
MERGE (p:Publication {pub_id: toInteger(nodeRecord.`PMID`)})
  ON CREATE
  SET p.url= genomiccore.resolvePubmedUrl(toString(p.pub_id)),
  p.needs_properties=true, p.needs_references=true
SET p:PubMed
MERGE (ge)-[r:HAS_PUBLICATION]->(p)
',
{batchSize:4000, iterateList:true});