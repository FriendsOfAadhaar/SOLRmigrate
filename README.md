SOLRmigrate
===========

Tools for SOLR indexing and migration from SOLR 3.x to 4.x


A scalable tool to do SOLR indexing of structured data from (multiple) csv files

1. Version of SOLR to be used: 4.9
2. Tool should be multi-threaded AND linearly scalable indexing through addition of instances
3. Eliminate non alpha numeric characters before indexing the record
4. Index alphabetical fields using soundex representation
5. Capability to not index certain words, in certain fields
6. Capability to mark records containing certain words
7. Created a sharded index with 16 shards based on first character of refID
8. Return limited fields (eid, refId, UID) from search results to optimize performance

