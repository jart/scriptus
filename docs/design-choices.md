There are several bad or questionable design choices in Scriptus that I will enumerate here.

#Filesystem in place of RDBMS for local storage

Ideally an RDBMS should be setup (maybe Jersey or just a JDBC connection) instead of using the filesystem. This may be done later.

The reason it wasn't done immediately is because the filesystem DAO grew out of the original command-line tools I used as my original proof of concept, I didn't have an RDBMS, I didn't want to learn JPA in this project, I originally thought I would need mostly BLOB storage, and I guessed a filesystem would be easier to convert to S3 for horizontal partitioning (a case of YAGNI).

#Using correlation IDs instead of the Twitter "in-reply-to" IDs for correlating tweets

Really no reason for this one, except that I spent a fun hour writing a method to output numbers of an arbitrary base for compressing the hashtags into the smallest possible space, and that I discovered the "in_reply_to_status_id" after I'd already implemented the correlation IDs. See also the duplicates issue below.

#Using stack-based correlation instead of IDs for ask(), like I do for listen()

Is not as reliable as the reply-ID above.

#Twitter stops duplicate tweets

This may be a problem if a user wants to send  an identical response to the same script that has been launched twice, e.g. during debugging. Responding to listen()s would have to be made unique using junk after the "//", where as ask() conveniently has the correlation IDs, see above. Solving a problem they weren't intended to.

