#Julia script to load the GenomicCore database
#Load the Cypher constraints for the core nodes
result1 = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/GenomicGraphCore/cql/CoreConstraints.cql`, String)
# HGNC
result2 = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/GenomicGraphCore/cql/load_hgnc.cql`, String)
hgncCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:Hgnc) return count(n) '`, String)
printf("%s Hgnc nodes loaded ", hgncCount)
println(result2)
# load UniProt data
result3 = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/GenomicGraphCore/cql/load_uniprot.cql`, String)
uniprotCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:UniProtEntry) return count(n) '`, String)
printf("%s UniProtEntry nodes loaded ", uniprotCount)
println(result3)
# Reactome
result4 = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/GenomicGraphCore/cql/load_uniprot_reactome.cql`, String)
reactCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:Reactome) return count(n) '`, String)
printf("%s Reactome nodes loaded ", uniprotCount)
println(result4)
# EntrezGene
result5= read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/GenomicGraphCore/cql/load_entrez_gene.cql`, String)
entrezCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:EntrezGene) return count(n) '`, String)
printf("%s EntrezGene nodes loaded ",entrezCount)
println(result5)
result6 = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/GenomicGraphCore/cql/load_entrez_gene_reactome.cql`, String)
reactCount2 = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:Reactome) return count(n) '`, String)
printf("%s Reactome nodes loaded ",reactCount2)
println(result6)
# NHGRI
result7 = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/GenomicGraphCore/cql/load_nhgri_gene.cql`, String)
nhgriCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:NHGRI_Gene) return count(n) '`, String)
printf("%s NHGRI_Gene nodes loaded ",nhgriCount)
println(result7)
# DisGenet
result8 = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/GenomicGraphCore/cql/load_disgenet.cql`, String)
disgenetCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:DisgenetGene) return count(n) '`, String)
printf("%s DisgenetGene nodes loaded ",disgenetCount)
println(result8)
# Load node relationships
result9 = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/GenomicGraphCore/cql/Core_Relationships.cql`, String)
# Create Publication placeholder nodes
result10 = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/GenomicGraphCore/cql/load_pubmed_ids.cql`, String)
pubCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:Publication) return count(n) '`, String)
printf("%s Publication nodesloaded ", pubCount)
println(result10)
