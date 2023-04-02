// Constraints and Indexes for GenomicGraphCore

CREATE CONSTRAINT unique_pubmed_id IF NOT EXISTS FOR (pub: Publication)  REQUIRE pub.pub_id IS UNIQUE;
CREATE CONSTRAINT unique_obosyn_id IF NOT EXISTS FOR (syn: GoSynonym)  REQUIRE syn.synonym_id IS UNIQUE;
CREATE CONSTRAINT unique_hgnc_id IF NOT EXISTS FOR (h:Hgnc)  REQUIRE h.hgnc_id IS UNIQUE;
CREATE CONSTRAINT unique_uniprot_id IF NOT EXISTS FOR (u:UniProtEntry)  REQUIRE u.entry_id IS UNIQUE;
CREATE CONSTRAINT unique_nhgri_gene_id IF NOT EXISTS FOR (gene:NHGRI_Gene) REQUIRE gene.gene_symbol IS UNIQUE;
CREATE CONSTRAINT unique_disgenet_gene_id IF NOT EXISTS FOR (gene:Disgenet_Gene) REQUIRE gene.gene_id IS UNIQUE;
CREATE CONSTRAINT unique_disgenet_disease_id IF NOT EXISTS FOR (dis:DisgenetDisease) REQUIRE dis.disease_id IS UNIQUE;
CREATE CONSTRAINT unique_disgenet_variant_snp_id IF NOT EXISTS FOR (var:DisgenetVariant) REQUIRE var.snp_id IS UNIQUE;
CREATE CONSTRAINT unique_entrez_gene_id IF NOT EXISTS FOR (entrez:EntrezGene) REQUIRE entrez.gene_id IS UNIQUE;
CREATE CONSTRAINT unique_reactome_id IF NOT EXISTS FOR (react:Reactome) REQUIRE react.reactome_id IS UNIQUE;
CREATE CONSTRAINT unique_genomic_entity_id IF NOT EXISTS FOR (ge:GenomicEntity) REQUIRE ge.gene_symbol IS UNIQUE;
CREATE CONSTRAINT unique_hgnc_gene_group IF NOT EXISTS FOR (hgg: HgncGeneGroup) REQUIRE hgg.gene_group IS UNIQUE;

CREATE INDEX hgnc_gene_symbol_index IF NOT EXISTS FOR (h:Hgnc) ON h.gene_symbol;
CREATE INDEX publication_doi_index IF NOT EXISTS FOR (p:Publication) ON p.doi_id;
create index uniprot_entry_name_index IF NOT EXISTS  FOR (u:UniProtEntry) ON u.entry_name;
create index disgenet_gene_gene_symbol_index IF NOT EXISTS FOR (d:Disgenet) ON d.gene_symbol;
CREATE INDEX entrez_gene_symbol_index IF NOT EXISTS FOR (entrez:EntrezGene) ON entrez.gene_symbol
