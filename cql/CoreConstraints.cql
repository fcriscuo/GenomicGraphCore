// Constraints and Indexes for GenomicGraphCore


CREATE CONSTRAINT unique_oboterm_id IF NOT EXISTS FOR (obo: OboTerm)  REQUIRE obo.obo_id IS UNIQUE;
CREATE CONSTRAINT unique_obosyn_coll_id IF NOT EXISTS FOR (syncoll: OboSynonymCollection)  REQUIRE syncoll.go_id IS UNIQUE;
CREATE CONSTRAINT unique_obo_xref_id IF NOT EXISTS FOR (xref: OboXref)  REQUIRE xref.xref_key IS UNIQUE;
CREATE CONSTRAINT unique_pubmed_id IF NOT EXISTS FOR (pub: Publication)  REQUIRE pub.pub_id IS UNIQUE;
CREATE CONSTRAINT unique_obosyn_id IF NOT EXISTS FOR (syn: GoSynonym)  REQUIRE syn.synonym_id IS UNIQUE;
CREATE CONSTRAINT unique_hgnc_id IF NOT EXISTS FOR (h:Hgnc)  REQUIRE h.hgnc_id IS UNIQUE;
CREATE CONSTRAINT unique_pub_id IF NOT EXISTS FOR (p:Publication)  REQUIRE p.pub_id IS UNIQUE;
CREATE CONSTRAINT unique_uniprot_id IF NOT EXISTS FOR (u:UniProtEntry)  REQUIRE u.entry_id IS UNIQUE;
CREATE CONSTRAINT unique_nhgri_gene_id IF NOT EXISTS FOR (gene:NHGRI_Gene) REQUIRE gene.gene_symbol IS UNIQUE;
CREATE CONSTRAINT unique_disgenet_gene_id IF NOT EXISTS FOR (gene:Dosgenet_Gene) REQUIRE gene.gene_id IS UNIQUE;

CREATE INDEX hgnc_gene_symbol_index FOR (h:Hgnc) ON h.gene_symbol;
CREATE INDEX publication_doi_index FOR (p:Publication) ON p.doi_id;
create index uniprot_entry_name_index  FOR (u:UniProtEntry) ON u.entry_name;
create index disgenet_gene_gene_symbol_index FOR (d:Disgenet) ON d.gene_symbol;