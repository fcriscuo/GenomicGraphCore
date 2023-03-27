CALL apoc.periodic.iterate('
CALL apoc.load.csv("Volumes/Dea5TBExt/GenomicCoreData/HGNC/hgnc_complete_set.tsv", { header:true,  sep:"TAB",
  mapping: {
  `alias_symbol`: {array:true,arraySep:"|", name:"alias_symbols"},
  `alias_name`: {array:true,arraySep:"|", name:"alias_names"},
  `gene_group`: {array:true,arraySep:"|", name:"gene_groups"},
  `ccds_id`: {array:true,arraySep:"|", name:"ccdc_ids"},
  `uniprot_ids`: {array:true,arraySep:"|", name:"uniprot_ids"},
  `pubmed_id`: {array:true,arraySep:"|", name:"pubmed_ids"},
  `bioparadigms_slc`: {array:true,arraySep:"|", name:"bioparadigms_slc"},
  `enzyme_id`: {array:true,arraySep:"|", name:"enzyme_ids"},
  `rna_central_idsv`: {array:true,arraySep:"|", name:"rna_central_ids"}
  }
}
)
YIELD nodeRecordNo, map AS nodeRecord RETURN nodeRecord, nodeRecordNo
','
WHERE NOT toInteger(trim(nodeRecord.`hgnc_id`)) IS NULL
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
hgnc.gene_group_id=toInteger(nodeRecord.gene_group_id),
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
hgnc.enzyme_ids=nodeRecord.enzyme_id,
hgnc.intermediate_filament_db=nodeRecord.intermediate_filament_db,
hgnc.rna_central_ids=nodeRecord.rna_central_ids,
hgnc.lncipedia=nodeRecord.lncipedia,
hgnc.gtrnadb=nodeRecord.gtrnadb,
hgnc.agr=nodeRecord.agr,
hgnc.mane_select=nodeRecord.mane_select,
hgnc.gencc=nodeRecord.gencc;
',
{batchSize:4000, iterateList:true, parallel:true});

