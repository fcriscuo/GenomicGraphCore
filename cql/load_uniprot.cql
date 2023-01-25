LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/GenomicCoreData/UniProt/uniprot_20Jan2023.tsv' AS line FIELDTERMINATOR '\t'
CALL {
WITH line
MERGE (uni:UniProtEntry {entry_id: line.Entry})

SET  uni.entry_name =line.`Entry Name`,
  uni.uniprot_emtry_url = replace("https://www.uniprot.org/uniprotkb/XXXX/entry","XXXX",uni.entry_id),
  uni.protein_names = coalesce(split(line.`Protein names`,';'),' '),
  uni.gene_names = coalesce(split(line.`Gene Names`,' '),' '),
  uni.length = toInteger(line.length),
  uni.gene_symbol = line.`Gene Names (primary)`,
  uni.interacts_with = coalesce(split(line.`Interacts with`,';'),' '),
  uni.gene_ontology_ids = coalesce(split(line.`Gene Ontology IDs`,';'),' '),
  uni.pubmed_ids=coalesce(split(line.`PubMed ID`,';'),' '),
  uni.embl_ids=coalesce(split(line.EMBL,';'),' '),
  uni.alpha_fold_id = replace(line.AlphaFoldDB,';',''),
  uni.pdb = replace(line.PDB,';',''),
  uni.dbSNPs = coalesce(split(line.dbSNP,' '),' '),
  uni.hgnc_id = replace(line.HGNC,';',''),
  uni.reactome_id = replace(line.Reactome,';',''),
  uni.pharos_id = replace(line.Pharos,';','')
} IN TRANSACTIONS OF 1000 ROWS;

MATCH (uni:UniProtEntry) WHERE uni.hgnc_id <> ' '
MATCH(hgnc:Hgnc{hgnc_id:uni.hgnc_id})
MERGE (uni) - [r:HAS_HGNC] -> (hgnc)
;

MATCH (uni:UniProtEntry) WHERE uni.pubmed_ids <> ' '
UNWIND uni.pubmed_ids AS pubmed_id
MERGE (p:Publication{pub_id:toInteger(ltrim(pubmed_id))})
SET p.url= genomiccore.resolvePubmedUrl(toString(p.pub_id))
MERGE (uni) - [r:HAS_PUBLICATION] -> (p)
;

MATCH (uni:UniProtEntry) WHERE uni.gene_ontology_ids <> ' '
UNWIND uni.gene_ontology_ids AS go_id
MATCH(obo: OboTerm) WHERE obo.obo_id = go_id
MERGE (uni) -[r:HAS_GENE_ONTOLOGY]-> (obo)
;

MATCH (uni:UniProtEntry) WHERE uni.interacts_with IS NOT NULL
UNWIND uni.interactswith AS target_id
MATCH (target:UniProtEntry) WHERE  target.entry_id = target_id
MERGE (uni) -[r:INTERACTS_WITH]- (target)
;

// AlphaFold URL
MATCH (uni:UniProtEntry) WHERE uni.alpha_fold_id  IS NOT NULL
SET uni.alphafold_url = genomiccore.resolveAlphaFoldUrl(uni.alpha_fold_id)

//dbSnp URLs
MATCH (uni:UniProtEntry) WHERE uni.dbSNPs IS NOT NULL
SET uni.dbSnpUrlList = []
UNWIND uni.dbSnps AS snp
SET uni.dbSnpList = uni.dbSnpList + genomiccore.resolvedbSnpUrl(snp)
;