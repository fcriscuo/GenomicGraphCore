// UniProt Entry
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/GenomicCoreData/UniProt/uniprot_20Jan2023.tsv", { header:true, sep:"TAB",
mapping:{
     `Protein names`: {array:true, arraySep:";", name:"protein_names"},
     `Gene Names`: {array:true, arraySep:";", name:"gene_names"},
     `Interacts with`: {array:true, arraySep:";", name:"interacts_with"},
     `Gene Ontology IDs`: {array:true, arraySep:";", name:"gene_ontology_ids"},
     `PubMed ID`: {array:true, arraySep:";", name:"pubmed_ids"},
     `EMBL`: {array:true, arraySep:";", name:"embl_ids"},
     `AlphaFoldDB`: {array:true, arraySep:";", name:"alpha_fold_ids"},
     `PDB`: {array:true, arraySep:";", name:"pdb"},
     `dbSNP`: {array:true, arraySep:";", name:"dbSNPs"},
     `HGNC`: {array:true, arraySep:";", name:"hgnc_ids"},
     `Reactome`: {array:true, arraySep:";", name:"reactome_id"},
     `Pharos`: {array:true, arraySep:";", name:"pharos_id"}
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
  uni.embl_ids=nodeRecord.EMBL,
  uni.alpha_fold_ids = nodeRecord.AlphaFoldDB,
  uni.pdb = nodeRecord.PDB,,
  uni.dbSNPs = nodeRecord.dbSNP,
  uni.hgnc_ids = nodeRecord.HGNC,
  uni.reactome_id = nodeRecord.Reactome,
  uni.pharos_id = nodeRecord.Pharos
',
{batchSize:1000, iterateList:true, parallel:true});

// set the gene_symbol property for a UniProt entry based on its
// related HGNC
MATCH (uni:UniProtEntry) WHERE uni.hgnc_id <> ' '
MATCH(hgnc:Hgnc{hgnc_id:uni.hgnc_id})
SET uni.gene_symbol = hgnc.gene_symbol
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