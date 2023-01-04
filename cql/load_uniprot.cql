 CALL {
LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/UniProt/sample2_uniprot_reviewed_29Dec2022.tsv' AS line FIELDTERMINATOR '\t'

MERGE (uni:UniProtEntry {entry_id: line.Entry})
SET  uni.entry_name =line.`Entry Name`,
  uni.protein_names = COALESCE(split(line.`Protein names`,';')," "),
  uni.gene_names = COALESCE(split(line.`Gene Names`,' ')," "),
  uni.length = toInteger(line.length),
  uni.gene_ontology_ids = COALESCE(split(line.`Gene Ontology IDs`,';')," "),
  uni.disease_involvement= line.`Involvement in disease`,
  uni.pubmed_ids=COALESCE(split(line.`PubMed ID`,';')," "),
  uni.alpha_fold_id = replace(line.AlphaFoldDB,';',''),
  uni.intact_id = line.IntACt,
  uni.kegg_id = replace(line.KEGG,';',''),
  uni.ucsc_id = replace(line.UCSC,';',''),
  uni.hgnc_id = replace(line.HGNC,';',''),
  uni.reactome_id = replace(line.Reactome,';',''),
  uni.pharos_id = replace(line.Pharos,';','')
} IN TRANSACTIONS OF 1000 ROWS;

match (uni:UniProtEntry) where uni.hgnc_id <> " "
MATCH(hgnc:Hgnc{hgnc_id:uni.hgnc_id})
merge (uni) - [r:HAS_HGNC] -> (hgnc)
;

match (uni:UniProtEntry) where uni.pubmed_ids <> " "
unwind uni.pubmed_ids AS pubmed_id
merge (p:Publication{pub_id:toInteger(ltrim(pubmed_id))})
SET p.url= genomiccore.resolvePubmedUrl(toString(p.pub_id))
merge (uni) - [r:HAS_PUBLICATION] -> (p)
;

match (uni:UniProtEntry) where uni.gene_ontology_ids <> " "
unwind uni.gene_ontology_ids AS go_id
match(obo: OboTerm) where obo.obo_id = go_id
merge (uni) -[r:HAS_GENE_ONTOLOGY]-> (obo)
;
