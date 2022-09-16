# GenomicGraphCore

### Introduction

**GenomicGraphCore** represents a Kotlin/Neo4j project that will aggregate genomic data from a collection of data sources and create a basic Neo4j database for these data. The application and database are intended to establish a framework for integrating data from more specialized sources. In particular, the Kotlin components provide a common mechanism for loading data into the Neo4j database.

### Application Design

The application will load data from what are considered to be primary genomic data repositories. The set of repositories to be mined is determined by settings in a configuration file. Typically these might include UniProt, GeneOntology, HGNC, & Entrez.
Inputs for database loading operations are generally comma or tab delimited files downloaded from the data source to the local file system.

The core library can then be supplemented by using the same ETL framework to load data from more specialized data sources. For example, the **CosmicGraphDb** project loads data downloaded from the Sanger Lab's Catalog of Somatic Mutations In Cancer (COSMIC) database.
The resulting extended Neo4j database can be further extended by using the **SynMIC** project to load data from the Synonmous Mutations In Cancer database

### Publicatuion Data

Most data sources provide references to supporting publication data, primarily PubMed identifiers. These identifiers are loaded into the Neo4j database as basic Publication nodes. These basic nodes can be enhanced with additional properties (*i.e.* Title, Author, Abstract, *etc.*) by using the primary application in the **PublicationDataImporter** project. This application scans the Neo4j database for Publication nodes that have not been enhanced and completes them by submititng RESTful requests to NCBI. This functionality was implemented as a separate process because NCBI enforces a request rate of 3/second (10/second for registered users). Accomodating this slow data retrieval rate impacted file-based data loading operations. A second factor is that connections to NCBI are often interrupted. 

