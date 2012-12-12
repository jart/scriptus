#!/bin/sh
#the below regexps remove
# - HTML characters
# - heading hashes
# - code samples
# - inline code
# - URLs
#from markdown-formatted code, as a prelude to spellchecking
cat _posts/*.md |
sed -e 's/<[/]*[a-z]*>/ /g' \
    -e 's/^[#]*//' \
    -e '/```/,/```/ s/.*//' \
    -e 's/`.*`//' \
    -e 's/\[\([^]]*\)\]([^)]*)/\1/g' \
      | ispell -p misc/words.list -l

