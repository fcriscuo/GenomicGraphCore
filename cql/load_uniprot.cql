// UniProt Entry
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/UniProt/uniprot_20Jan2023.tsv", { header:true, sep:"TAB",
mapping:{
     `Protein names`: {array:true, arraySep:";"},
     `Gene Names`: {array:true, arraySep:";"},
     `Interacts with`: {array:true, arraySep:";"},
     `Gene Ontology IDs`: {array:true, arraySep:";"},
     `PubMed ID`: {array:true, arraySep:";"},
     `EMBL`: {array:true, arraySep:";"},
     `AlphaFoldDB`: {array:true, arraySep:";"},
     `PDB`: {array:true, arraySep:";"},
     `dbSNP`: {array:true, arraySep:";"},
     `HGNC`: {array:true, arraySep:";"},
     `Reactome`: {array:true, arraySep:";"},
     `Pharos`: {array:true, arraySep:";"}
     }
}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT nodeRecord.`Entry` IS NULL
MERGE (uni:UniProtEntry {entry_id: nodeRecord.Entry})
SET  uni.entry_name =nodeRecord.`Entry Name`,
  uni.uniprot_emtry_url = replace("https://www.uniprot.org/uniprotkb/XXXX/entry","XXXX",uni.entry_id),
  uni.protein_names = nodeRecord.`Protein names`,
  uni.gene_names = nodeRecord.`Gene Names`,
  uni.length = toInteger(nodeRecord.length),
  uni.gene_symbol = nodeRecord.`Gene Names (primary)`,
  uni.interacts_with = nodeRecord.`Interacts with`,
  uni.gene_ontology_ids = nodeRecord.`Gene Ontology IDs`,
  uni.pubmed_ids=nodeRecord.`PubMed ID`,
  uni.embl_ids=nodeRecord.`EMBL`,
  uni.alpha_fold_ids = nodeRecord.`AlphaFoldDB`,
  uni.pdb = nodeRecord.`PDB`,
  uni.dbSNPs = nodeRecord.`dbSNP`,
  uni.hgnc_ids = nodeRecord.`HGNC`,
  uni.reactome_id = nodeRecord.`Reactome`,
  uni.pharos_id = nodeRecord.`Pharos`
',
{batchSize:1000, iterateList:true});

// set the gene_symbol property for a UniProt entry based on its
// related HGNC
MATCH (uni:UniProtEntry) WHERE uni.hgnc_ids  IS NOT NULL
MATCH(hgnc:Hgnc) WHERE hgnc.hgnc_id = uni.hgnc_ids[0]
SET uni.gene_symbol = hgnc.gene_symbol
;

// AlphaFold URL
MATCH (uni:UniProtEntry) WHERE uni.alpha_fold_ids  IS NOT NULL
SET uni.alpha_fold_urls = []
WITH uni
UNWIND uni.alpha_folds_ids as alpha_id
WITH alpha_id, uni
SET uni.alpha_fold_urls = uni.alpha_fold_urls + genomiccore.resolveAlphaFoldUrl(alpha_id)
;

//dbSnp URLs
MATCH (uni:UniProtEntry) WHERE uni.dbSNPs IS NOT NULL
SET uni.dbSnpUrlList = []
WITH uni
UNWIND uni.dbSnps AS snp
SET uni.dbSnpList = uni.dbSnpList + genomiccore.resolvedbSnpUrl(snp)
;

// UniProtEntry -> UniProtEntry relationship (self-interaction)
MATCH (uni:UniProtEntry) WHERE uni.interacts_with IS NOT NULL
UNWIND uni.interacts_with AS target_id
WITH uni, target_id
MATCH (target:UniProtEntry) WHERE  target.entry_id = target_id
MERGE (uni)-[r:INTERACTS_WITH]->(target)
;