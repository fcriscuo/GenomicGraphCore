:auto USING PERIODIC COMMIT 1000
LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/HGNC/hgnc_complete_set.tsv' AS line FIELDTERMINATOR '\t'

MERGE (hgnc:Hgnc {hgnc_id: line.hgnc_id})

SET hgnc.gene_symbol=line.symbol,
    hgnc.gene_name=line.name,
    hgnc.locus_group=line.locus_group,
    hgnc.locus_type=line.locus_type,
    hgnc.location=line.location,
    hgnc.location_sortable=line.location_sortable,
    hgnc.alias_symbols=COALESCE(split(line.alias_symbol,'|')," "),
    hgnc.alias_names=COALESCE(split(line.alias_name,'|')," "),
    hgnc.gene_groups=COALESCE(split(line.gene_group,'|')," "),
    hgnc.gene_group_id=toInteger(line.gene_group_id),
    hgnc.entrez_id=line.entrez_id,
    hgnc.ensembl_gene_id=line.ensembl_gene_id,
    hgnc.vega_id=line.vega_id,
    hgnc.ucsc_id=line.ucsc_id,
    hgnc.refseq_accession=line.refseq_accession,
    hgnc.ccds_ids=COALESCE(split(line.ccds_id,'|')," "),
    hgnc.uniprot_ids=COALESCE(split(line.uniprot_ids,'|')," "),
    hgnc.pubmed_ids=COALESCE(split(line.pubmed_id,'|')," "),
    hgnc.mgd_id=line.mgd_id,
    hgnc.rgd_id=line.rgd_id,
    hgnc.lsdb=line.lsdb,
    hgnc.cosmic=line.cosmic,
    hgnc.omim_id=line.omim_id,
    hgnc.mirbase=line.mirbase,
    hgnc.homeodd=line.homeodb,
    hgnc.snornabase=line.snornabase,
    hgnc.bioparadigms_slc=COALESCE(split(line.bioparadigms_slc,'|')," "), 
    hgnc.orphanet=line.orphanet,
    hgnc.pseudogene_org=line.`pseudogene.org`,
    hgnc.horde_id=line.horde_id,
    hgnc.merops=line.merops,
    hgnc.imgt=line.imgt,
    hgnc.iuphar=line.iuphar,
    hgnc.kznf_gene_catalog=line.kznf_gene_catalog,
    hgnc.mamit_trnadb=line.`mamit-trnadb`,
    hgnc.cd=line.cd,
    hgnc.lncrnadb=line.lncrnadb,
    hgnc.enzyme_ids=COALESCE(split(line.enzyme_id,'|')," "),
    hgnc.intermediate_filament_db=line.intermediate_filament_db,
    hgnc.rna_central_ids=COALESCE(split(line.rna_central_ids,'|')," "),
    hgnc.lncipedia=line.lncipedia,
    hgnc.gtrnadb=line.gtrnadb,
    hgnc.agr=line.agr,
    hgnc.mane_select=line.mane_select,
    hgnc.gencc=line.gencc,
    hgnc.created=datetime()
    ;

    match (h:Hgnc) where h.pubmed_ids <> " "
    unwind h.pubmed_ids AS pubmed_id 
    merge (p:Publication{pub_id:toInteger(pubmed_id)})
    merge (h) - [r:HAS_PUBLICATION] -> (p)
    ;