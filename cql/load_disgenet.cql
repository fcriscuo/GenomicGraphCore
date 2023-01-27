:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/GenomicCoreData/DisGeNet/gene_associations.tsv' AS line FIELDTERMINATOR '\t'
CALL {WITH line
MERGE (gene:DisgenetGene:Gene{ncbi_gene_id: line.geneId})

SET gene.gene_symbol = line.geneSymbol,
  gene.ncbi_gene_url = genomiccore.resolveNcbiGeneUrl(ltrim(line.geneId)),
  gene.dsi = toFloat(line.DSI),
  gene.dpi = toFloat(line.DPI),
  gene.pli = toFloat(line.PLI),
  gene.protein_clas_name = line.protein_clas_name,
  gene.protein_class = line.protein_class,
  gene.number_of_diseases = toInteger(line.NofDiseases),
  gene.number_of_pmids = toInteger(line.NofPmids),
  gene.date_created = datetime()
} IN TRANSACTIONS OF 3000 ROWS;

MATCH (gene:DisgenetGene) WHERE gene.gene_symbol IS NOT NULL
MATCH(hgnc:Hgnc) WHERE hgnc.gene_symbol = gene.gene_symbol
MERGE (gene) - [r:HAS_HGNC] -> (hgnc)
;

// relationship to UniProt via mapa_geneid_4_uniprot_crossref.tsv
:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/GenomicCoreData/DisGeNet/mapa_geneid_4_uniprot_crossref.tsv' AS line FIELDTERMINATOR '\t'
CALL {
WITH line
MATCH (gene:DisgenetGene) WHERE ltrim(gene.ncbi_gene_id) = ltrim(line.GENEID)
MATCH (uni:UniProtEntry) WHERE uni.entry_id = line.UniProtKB
MERGE (gene)-[r1:HAS_UNIPROT_ENTRY]->(uni)
} IN TRANSACTIONS OF 3000 ROWS;

// DisGeNet Variants
:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/GenomicCoreData/DisGeNet/variant_associations.tsv' AS line FIELDTERMINATOR '\t'
CALL {
WITH line
MERGE (var:DisgenetVariant {snp_id: line.snpId})
SET var.class = line.class,
var.dbsnp_url = genomiccore.resolvedbSnpUrl(line.snpId),
var.chromosome = line.chromosome,
var.position = toInteger(line.position),
var.most_severe_consequence = line.most_severe_consequence,
var.dsi = toFloat(line.DSI),
var.dpi = toFloat(line.DPI),
var.number_of_diseases = toInteger(line.NofDiseases),
var.number_of_pmids = toInteger(line.NofPmids),
var.date_created = datetime()
}IN TRANSACTIONS OF 3000 ROWS;

// relationship to DisgenetGene in variant_to_gene_mapping.tsv
:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/GenomicCoreData/DisGeNet/dbsnp_variant_to_gene_mappings.tsv' AS line FIELDTERMINATOR '\t'
CALL {
WITH line
MATCH(gene:DisgenetGene) WHERE gene.gene_symbol = line.geneSymbol
MATCH(var:DisgenetVariant) WHERE var.snp_id = line.snpId
MERGE (gene)-[r1:HAS_DISGENET_VARIANT{source:line.sourceId}]->(var)
}IN TRANSACTIONS OF 3000 ROWS;

//DisGeNet Diseases
:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/GenomicCoreData/DisGeNet/disease_mappings.tsv' AS line FIELDTERMINATOR '\t'
CALL {
WITH line
MERGE (dis:DisgenetDisease {disease_id: line.diseaseId})
ON CREATE SET dis.disease_name = line.name,
  dis.vocabulary = line.vocabulary,
  dis.code = line.code,
  dis.vocabulary_names = split(line.vocabularyName,';'),
  dis.date_created = datetime()
ON MATCH SET dis.vocabulary_names = dis.vocabulary_names + line.vocabularyName
}IN TRANSACTIONS OF 3000 ROWS;

// if the node is an HPO term, create a relationship to an HpTerm
match (d:DisgenetDisease) WHERE d.vocabulary = 'HPO'
MATCH (hp:HpTerm) WHERE hp.obo_id = d.code
MERGE (d) -[r:HAS_HPO_TERM] ->(hp);

// supplement properties with data from disease_mappings_to_attributes.tsv file
//diseaseId	name	type	diseaseClassMSH	diseaseClassNameMSH	hpoClassId	hpoClassName	doClassId	doClassName	umlsSemanticTypeId	umlsSemanticTypeName
:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/GenomicCoreData/DisGeNet/disease_mappings_to_attributes.tsv' AS line FIELDTERMINATOR '\t'
CALL {
WITH line
MERGE (dis:DisgenetDisease {disease_id: line.diseaseId})
SET dis.disease_type = line.type,
  dis.disease_class_msh = split(line.diseaseClassMSH,';'),
  dis.disease_class_name_msh = split(line.diseaseClassNameMSH,';'),
  dis.hpo_class_id = split(line.hpoClassId,';'),
  dis.hpo_class_name = split(line.hpoClassName,';'),
  dis.do_class_id = split(line.doClassId,';'),
  dis.do_class_name = split(line.doClassName,';'),
  dis.umls_semantic_type_id = line.umlsSemanticTypeId,
  dis.umls_semantic_type_name = line.umlsSemanticTypeName,
  dis.last_mod_date = datetime()
}IN TRANSACTIONS OF 3000 ROWS;

// supplement properties with data from disease_associations.tsv file
:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/GenomicCoreData/DisGeNet/disease_associations.tsv' AS line FIELDTERMINATOR '\t'
CALL {
WITH line
MERGE (dis:DisgenetDisease {disease_id: line.diseaseId})
ON MATCH SET dis.disease_type = line.diseaseType,
  dis.disease_semantic_type = line.diseaseSemanticType,
  dis.number_of_genes = toInteger(line.NofGenes),
  dis.number_of_pmids = toInteger(line.NofPmids),
  dis.date_mod_date= datetime()
}IN TRANSACTIONS OF 3000 ROWS;