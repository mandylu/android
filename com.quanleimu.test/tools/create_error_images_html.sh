#!/bin/sh
# This is a comment

#ls -lA
cd ../logs/screen/;

FILE_LIST="`ls *.png`"

RESULT="List of images:<br /><br />"
for file in ${FILE_LIST}
do
    RESULT="<a href=\"${file}\">${file}</a>
<br />${RESULT}";

done

echo ${RESULT} > index.html

cd ../../source_git