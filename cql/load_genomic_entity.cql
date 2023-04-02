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

// Add the cytogenetic location to Genomic Entity
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