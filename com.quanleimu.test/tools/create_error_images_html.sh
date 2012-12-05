#!/bin/sh
# This is a comment

#ls -lA
cd ../logs/screen/;

FILE_LIST="`ls *.png`"

RESULT="List of images:"
for file in ${FILE_LIST}
do
    RESULT="${RESULT}<br />
<a href=\"${file}\">${file}</a>";

done

echo ${RESULT} > index.html

cd ../../source_git