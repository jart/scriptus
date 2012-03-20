There are several bad or questionable design choices in Scriptus that I will enumerate here.

#Filesystem in place of RDBMS for local storage

Ideally an RDBMS should be setup (maybe Jersey or just a JDBC connection) instead of using the filesystem. This may be done later.

The reason it wasn't done immediately is because the filesystem datastore grew out of the original command-line tools I used as my original proof of concept, I didn't have an RDBMS, I didn't want to learn JPA in this project, I originally thought I would need mostly BLOB storage, and I guessed a filesystem would be easier to convert to S3 for horizontal partitioning (a case of YAGNI).

#Twitter stops duplicate tweets

This may be a problem if a user wants to send  an identical response to the same script that has been launched twice, e.g. during debugging. Responding to listen()s would have to be made unique using junk after the "//".

