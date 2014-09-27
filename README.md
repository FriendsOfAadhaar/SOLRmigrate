SOLRmigrate
===========

Tools for SOLR indexing and migration from SOLR 3.x to 4.x

Java tool to create SOLR index of structured data from (multiple) csv files


Features:

1. Version of SOLR to be used: 4.9
2. Tool should be multi-threaded AND linearly scalable indexing through addition of instances
3. Eliminate non alpha numeric characters before indexing the record
4. Use soundex (or variant) representation to index alphabetical fields
5. Capability to not index certain word
6. Capability to mark records containing certain words
7. Created a sharded index with 16 shards based on first character of <id1>
8. Return limited fields (id1,id2,id3) from search results to optimize performance
9. Capability to skip indexing of some columns
10. Capability to accept columns in different order

CSV file format:

id1,id2,id3,name,gender,dob,address,vtc,subdistrict,district,state,pincode,mobile,email

id1: 36 digit UUID
id2: 28 digit numeric id
id3: 12 digit numeric id
name: string, eliminate nonalphanumber, token, index both (tokens and soundex(token)) 
dob: date
address: string
             - eliminate nonalphanumeric, 
             - tokenize
             - check not_tokenize_list
             - check mark_list
             - index both (tokens and soundex(token)
             
vtc: string
             - alternate_string_map
subdistrict: string
             - alternate_string_map
district: string
             - alternate_string_map
state: string
pincode: 6 digit number
mobile: 10 digit number (check structure)
email: string (check structure)
             
 
