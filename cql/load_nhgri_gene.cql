:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/GenomicCoreData/NHGRI/NCBI_Clinical_Genomic_Database.tsv' AS line FIELDTERMINATOR '\t'
CALL {
WITH line
MERGE (gene:NHGRI_Gene:Gene{gene_symbol: line.GENE})

SET gene.hgnc_id = apoc.text.join(["HGNC",line.`HGNC ID`],':'),
  gene.nhgri_gene_url = replace("https://research.nhgri.nih.gov/CGD/view/?t=XXX&par=general:gene,conditions:manifestation,intervention:All&g=XXXX","XXXX",
gene.gene_symbol),
  gene.entrez_id = line.`ENTREZ GENE ID`,
  gene.entrez_gene_url= replace("https://www.ncbi.nlm.nih.gov/gene/?term=XXXX","XXXX",gene.entrez_id),
  gene.conditions = split(line.CONDITION,';'),
  gene.inheritance = line.INHERITANCE,
  gene.age_group = line.`AGE GROUP`,
  gene.allelic_conditions = split(line.`ALLELIC CONDITIONS`,';'),
  gene.manifestation_categories = split(line.`MANIFESTATION CATEGORIES`,';'),
  gene.intervention_categories = split(line.`INTERVENTION CATEGORIES`,';'),
  gene.comments = line.COMMENTS,
  gene.intervention_rationales = split(line.`INTERVENTION/RATIONALE`,';'),
  gene.pub_ids = split(line.REFERENCES,';')
  gene.date_created= datetime()
} IN TRANSACTIONS OF 3000 ROWS;

// create relationships to HGNC nodes
MATCH (gene:NHGRI_Gene) WHERE gene.hgnc_id IS NOT NULL
MATCH (hgnc:Hgnc) WHERE hgnc.hgnc_id = gene.hgnc_id
MERGE (gene)-[r1:HAS_HGNC]->(hgnc)

//create relationships to Publication nodes
match (gene:NHGRI_Gene) where gene.pub_ids IS NOT NULL
unwind gene.pub_ids AS pubmed_id
merge (p:Publication{pub_id:pubmed_id})
SET p.url= genomiccore.resolvePubmedUrl(pubmed_id)
merge (gene) - [r2:HAS_PUBLICATION] -> (p)
;