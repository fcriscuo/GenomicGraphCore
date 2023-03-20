LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/GenomicCoreData/HGNC/hgnc_complete_set.tsv' AS line FIELDTERMINATOR '\t'
RETURN count(line)