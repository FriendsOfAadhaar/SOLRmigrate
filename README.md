SOLRmigrate
===========

Tools for SOLR indexing and migration from SOLR 3.x to 4.x

Java tool to create SOLR index of structured data from csv files. Should be scalable to one billion records


Features:

1. Version of SOLR to be used: 4.10.2
2. Tool should be multi-threaded AND linearly scalable indexing through addition of instances. The goal is to minimize the time to index one billion records
3. Eliminate all non alpha numeric characters before indexing the record
4. Use double metapohone representation (in addition to actual text) to index alphabetical fields
5. Capability to not index certain word (not needed in intial release) 
6. Capability to mark records containing certain words (read list of words into a map, which is checked during indexing, an alert record csv is written for this record, not in initial release)
7. Created a sharded index with 16 shards based on first character of id1 (use SolrCloud for the same, allow explicit sharding from first character)
8. Return limited fields (id1,id2,id3) from search results to optimize performance
9. Capability to skip indexing of some columns 
10. Capability to accept columns in different order (not in intial release)
11. Replication as needed for redundancy/search throughput

CSV file format:

id1,id2,id3,res_name,res_dob_ddmmyyyy,res_gender,res_mobile_num,res_email_id,res_lang_code,biometric_flag,info_shre_cnsnt,issue_date,res_addr_careof,res_addr_building,res_addr_street,res_addr_landmark,res_addr_locality,res_addr_vtc_name,res_addr_district_name,res_addr_subdistrict_name,res_addr_state_name,res_addr_po_name,res_addr_pincode


id1: 36 digit UUID
id2: 28 digit numeric id
id3: 12 digit numeric id
res_name: string
             - eliminate nonalphanumeric, 
             - tokenize
             - check not_tokenize_list
             - check mark_list
             - index both (tokens and soundex(token)
res_dob_ddmmyy: date
res_gender: [M|F]
res_mobile_num: 10 digit number (check structure)
res_email_id: string (check structure)
res_lang_code: int
biometric_flag: int
info_shre_cnsnt: int
issue_date: datetime

res_addr_careof, res_addr_building, res_addr_street, res_addr_landmark, res_addr_localty: string
             - eliminate nonalphanumeric, 
             - tokenize
             - check not_tokenize_list
             - check mark_list
             - index both (tokens and soundex(token)
             
res_addr_vtc_name: string
             - alternate_string_map
res_addr_subdistrict_name: string
             - alternate_string_map
res_addr_district_name: string
             - alternate_string_map
res_addr_state_name: string
res_addr_po_name: string
              - alternate_string_map
res_addr_pincode: 6 digit number
