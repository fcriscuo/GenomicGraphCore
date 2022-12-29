// Constraints and Indexes for GenomicGraphCore


CREATE CONSTRAINT unique_oboterm_id IF NOT EXISTS ON (obo: OboTerm) ASSERT obo.obo_id IS UNIQUE;
CREATE CONSTRAINT unique_obosyn_coll_id IF NOT EXISTS ON (syncoll: OboSynonymCollection) ASSERT syncoll.go_id IS UNIQUE;
CREATE CONSTRAINT unique_obo_xref_id IF NOT EXISTS ON (xref: OboXref) ASSERT xref.xref_key IS UNIQUE;
CREATE CONSTRAINT unique_pubmed_id IF NOT EXISTS ON (pub: Publication) ASSERT pub.pub_id IS UNIQUE;
CREATE CONSTRAINT unique_obosyn_id IF NOT EXISTS ON (syn: GoSynonym) ASSERT syn.synonym_id IS UNIQUE;
CREATE CONSTRAINT unique_hgnc_id IF NOT EXISTS on (h:Hgnc) ASSERT h.hgnc_id IS UNIQUE;
CREATE CONSTRAINT unique_pub_id IF NOT EXISTS on (p:Publication) ASSERT p.pub_id IS UNIQUE;
CREATE CONSTRAINT unique_uniprot_id IF NOT EXISTS on (u:UniProtEntry) ASSERT u.entry_id IS UNIQUE;

CREATE INDEX ON :Hgnc(gene_symbol);
CREATE INDEX ON :Publication(doi_id)
CREATE INDEX ON :UniProtEntry(entry_name)