//DisGenet Gene Associations
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/DisGeNet/gene_associations.tsv",
{ header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT nodeRecord.geneId IS NULL
MERGE (gene:DisgenetGene:Gene{ncbi_gene_id: nodeRecord.geneId})
SET gene.gene_symbol = nodeRecord.geneSymbol,
  gene.ncbi_gene_url = genomiccore.resolveNcbiGeneUrl(ltrim(nodeRecord.geneId)),
  gene.dsi = toFloat(nodeRecord.DSI),
  gene.dpi = toFloat(nodeRecord.DPI),
  gene.pli = toFloat(nodeRecord.PLI),
  gene.protein_clas_name = nodeRecord.protein_clas_name,
  gene.protein_class = nodeRecord.protein_class,
  gene.number_of_diseases = toInteger(nodeRecord.NofDiseases),
  gene.number_of_pmids = toInteger(nodeRecord.NofPmids)
',
{batchSize:4000, iterateList:true, parallel:true});


// DisGeNet Variants
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/DisGeNet/variant_associations.tsv", { header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT nodeRecord.snpId IS NULL
MERGE (var:DisgenetVariant {snp_id: nodeRecord.snpId})
SET var.class = nodeRecord.class,
var.dbsnp_url = genomiccore.resolvedbSnpUrl(nodeRecord.snpId),
var.chromosome = nodeRecord.chromosome,
var.position = toInteger(nodeRecord.position),
var.most_severe_consequence = nodeRecord.most_severe_consequence,
var.dsi = toFloat(nodeRecord.DSI),
var.dpi = toFloat(nodeRecord.DPI),
var.number_of_diseases = toInteger(nodeRecord.NofDiseases),
var.number_of_pmids = toInteger(nodeRecord.NofPmids)
',
{batchSize:4000, iterateList:true, parallel:true});

// DisGenet Disease Mappings
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/DisGeNet/disease_mappings.tsv",
{ header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT nodeRecord.diseaseId IS NULL
MERGE (dis:DisgenetDisease {disease_id: nodeRecord.diseaseId})
SET dis.disease_name = nodeRecord.name,
  dis.vocabulary = nodeRecord.vocabulary,
  dis.code = nodeRecord.code,
  dis.vocabulary_name = nodeRecord.vocabularyName
',
{batchSize:4000, iterateList:true, parallel:true});

// supplement properties with data from disease_mappings_to_attributes.tsv file
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/DisGeNet/disease_mappings_to_attributes.tsv",
{ header:true,  sep:"TAB",
mapping{
  `diseaseClassNameMSH`: {array:true, arraySep:";", name:"disease_class_name_msh",
  `diseaseClassMSH`: {array:true, arraySep:";", name:"disease_class_msh",
  `hpoClassId`: {array:true, arraySep:";", name:"hpo_class_ids",
  `hpo_class_name`: {array:true, arraySep:";", name:"hpo_class_names",
  `doClassName`: {array:true, arraySep:";", name:"do_class_names",
  `doClassId`: {array:true, arraySep:";", name:"do_class_ids"
  }
}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT nodeRecord.diseaseId IS NULL
MERGE (dis:DisgenetDisease {disease_id: nodeRecord.diseaseId})
SET dis.disease_type = nodeRecord.type,
  dis.disease_class_msh = nodeRecord.diseaseClassMSH,
  dis.disease_class_name_msh =nodeRecord.diseaseClassNameMSH,
  dis.hpo_class_ids =nodeRecord.hpoClassId,
  dis.hpo_class_names = nodeRecord.hpoClassName,
  dis.do_class_ids = nodeRecord.doClassId,
  dis.do_class_names = nodeRecord.doClassName,
  dis.umls_semantic_type_id = nodeRecord.umlsSemanticTypeId,
  dis.umls_semantic_type_name = nodeRecord.umlsSemanticTypeName
',
{batchSize:4000, iterateList:true, parallel:true});

// supplement properties with data from disease_associations.tsv file
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/DisGeNet/disease_associations.tsv",
{ header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT nodeRecord.diseaseId IS NULL
MERGE (dis:DisgenetDisease {disease_id: nodeRecord.diseaseId})
ON MATCH SET dis.disease_type = nodeRecord.diseaseType,
  dis.disease_semantic_type = nodeRecord.diseaseSemanticType,
  dis.number_of_genes = toInteger(nodeRecord.NofGenes),
  dis.number_of_pmids = toInteger(nodeRecord.NofPmids),
',
{batchSize:4000, iterateList:true, parallel:true});

