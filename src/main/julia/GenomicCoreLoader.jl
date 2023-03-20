#Julia script to load the GenomicCore database
#Load the Cypher constraints for the core nodes
results1 = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/GenomicGraphCore/cql/CoreConstraints.cql`, String)
# HGNC
results2 = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/GenomicGraphCore/cql/load_hgnc.cql`, String)
# load UniProt data
results3 = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/GenomicGraphCore/cql/load_uniprot.cql`, String)
# Reactome
results4 = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/GenomicGraphCore/cql/load_uniprot_reactome.cql`, String)
# EntrezGene
results5= read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/GenomicGraphCore/cql/load_entrez_gene.cql`, String)
results6 = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/GenomicGraphCore/cql/load_entrez_gene_reactome.cql`, String)
# NHGRI
results7 = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/GenomicGraphCore/cql/load_nhgri_gene.cql`, String)
# DisGenet
results8 = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/GenomicGraphCore/cql/load_disgenet.cql`, String)
# Load node relationships
results8 = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/GenomicGraphCore/cql/Core_Relationships.cql`, String)
# Create Publication placeholder nodes
results9 = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/GenomicGraphCore/cql/load_pubmed_ids.cql`, String)

