SOLRmigrate
===========

Tools for SOLR indexing and migration from SOLR 3.x to 4.x

A scalable tool to do SOLR indexing of csv files

TODO: Attach a sample csv to be indexed


1. Eliminate non alpha numeric characters before index record
2. Index alphabetical fields using soundex representation
3. Created a sharded index with 16 shards based on first character of refID
4. Return only limited fields from search results to optimize performance
5. Tool should be linearly scale indexing through addition of instances
