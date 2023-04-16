CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/HGNC/hgnc_complete_set.tsv", { header:true,  sep:"TAB",
mapping: {
  `alias_symbol`: {array:true,arraySep:"|"},
  `alias_name`: {array:true,arraySep:"|"},
  `gene_group`: {array:true,arraySep:"|"},
  `ccds_id`: {array:true,arraySep:"|"},
  `uniprot_ids`: {array:true,arraySep:"|"},
  `pubmed_id`: {array:true,arraySep:"|"},
  `bioparadigms_slc`: {array:true,arraySep:"|"},
  `enzyme_id`: {array:true,arraySep:"|"},
  `rna_central_idsv`: {array:true,arraySep:"|"}
  }
}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT nodeRecord.`hgnc_id` IS NULL
MERGE (hgnc:Hgnc {hgnc_id: nodeRecord.hgnc_id})
SET hgnc.gene_symbol=nodeRecord.symbol,
hgnc.gene_name=nodeRecord.name,
hgnc.locus_group=nodeRecord.locus_group,
hgnc.locus_type=nodeRecord.locus_type,
hgnc.location=nodeRecord.location,
hgnc.location_sortable=nodeRecord.location_sortable,
hgnc.alias_symbols=nodeRecord.alias_symbol,
hgnc.alias_names=nodeRecord.alias_name,
hgnc.gene_groups=nodeRecord.gene_group,
hgnc.gene_group_ids=nodeRecord.gene_group_id,
hgnc.entrez_id=nodeRecord.entrez_id,
hgnc.ensembl_gene_id=nodeRecord.ensembl_gene_id,
hgnc.vega_id=nodeRecord.vega_id,
hgnc.ucsc_id=nodeRecord.ucsc_id,
hgnc.refseq_accession=nodeRecord.refseq_accession,
hgnc.ccds_ids=nodeRecord.ccds_id,
hgnc.uniprot_ids=nodeRecord.uniprot_ids,
hgnc.pubmed_ids=nodeRecord.pubmed_id,
hgnc.mgd_id=nodeRecord.mgd_id,
hgnc.rgd_id=nodeRecord.rgd_id,
hgnc.lsdb=nodeRecord.lsdb,
hgnc.cosmic=nodeRecord.cosmic,
hgnc.omim_id=nodeRecord.omim_id,
hgnc.mirbase=nodeRecord.mirbase,
hgnc.homeodd=nodeRecord.homeodb,
hgnc.snornabase=nodeRecord.snornabase,
hgnc.bioparadigms_slc=nodeRecord.bioparadigms_slc,
hgnc.orphanet=nodeRecord.orphanet,
hgnc.pseudogene_org=nodeRecord.`pseudogene.org`,
hgnc.horde_id=nodeRecord.horde_id,
hgnc.merops=nodeRecord.merops,
hgnc.imgt=nodeRecord.imgt,
hgnc.iuphar=nodeRecord.iuphar,
hgnc.kznf_gene_catalog=nodeRecord.kznf_gene_catalog,
hgnc.mamit_trnadb=nodeRecord.`mamit-trnadb`,
hgnc.cd=nodeRecord.cd,
hgnc.lncrnadb=nodeRecord.lncrnadb,
hgnc.enzyme_ids=nodeRecord.enzyme_ids,
hgnc.intermediate_filament_db=nodeRecord.intermediate_filament_db,
hgnc.rna_central_ids=nodeRecord.rna_central_id,
hgnc.lncipedia=nodeRecord.lncipedia,
hgnc.gtrnadb=nodeRecord.gtrnadb,
hgnc.agr=nodeRecord.agr,
hgnc.mane_select=nodeRecord.mane_select,
hgnc.gencc=nodeRecord.gencc
',
{batchSize:4000, iterateList:true});

// Hgnc -> HgncGeneGroup relationship
MATCH (h:Hgnc) WHERE h.gene_groups IS NOT NULL
UNWIND h.gene_groups AS group
WITH group
WHERE group IS NOT NULL AND group <> ''
MERGE (hg:HgncGeneGroup{gene_group: group})
MERGE (h)-[r:BELONGS_TO_GENE_GROUP]->(hg)

// Add gene description property from Alliance of Genome Resources
// https://www.alliancegenome.org/downloads
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/Alliance/GENE-DESCRIPTION-TSV_HUMAN.tsv", { header:true,  sep:"TAB"}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT nodeRecord.`GeneId` IS NULL
MATCH (h:Hgnc) WHERE h.hgnc_id = nodeRecord.`GeneId`
SET h.gene_description = nodeRecord.`GeneDescription`
',
{batchSize:4000, iterateList:true });

// Add Hgnc -[association_type]-> DiseaseOntology
// from Alliance data
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/Alliance/DISEASE-ALLIANCE_HUMAN_25.tsv", { header:true,  sep:"TAB"})
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT nodeRecord.`DBObjectID` IS NULL
MATCH (h:Hgnc) WHERE h.hgnc_id = nodeRecord.`DBObjectID`
MATCH (do:DiseaseOntologyTerm) where do.obo_id = nodeRecord.`DOID`
MERGE (h)-[r:HAS_DISEASE_ASSOCIATION { association_type: nodeRecord.`AssociationType`}]->(do)
',
{batchSize:4000, iterateList:true });

// Supplement corresponding GenomicEntity collection of Publication/PubMed relationships
// create a Publication/PubMed placeholder node if pub_id is novel
match (h:Hgnc) where h.pubmed_ids <> " "
unwind h.pubmed_ids AS pubmed_id
WITH pubmed_id, h
  WHERE pubmed_id IS NOT NULL AND pubmed_id <> ''
merge (p:Publication:PubMed{pub_id:toInteger(pubmed_id)})
  ON CREATE
  SET p.url= genomiccore.resolvePubmedUrl(toString(p.pub_id)),
  p.needs_properties=true, p.needs_references=true
MATCH (ge:GenomicEntity) WHERE ge.gene_symbol = hjnc.gene_symbol
merge (ge)-[r:HAS_PUBLICATION]->(p)
;
