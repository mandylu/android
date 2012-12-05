#!/bin/sh
# This is a comment

#ls -lA
cd ../logs/screen/;

FILE_LIST="`ls *.png`"

RESULT=""
for file in ${FILE_LIST}
do
    RESULT="<a href=\"${file}\">${file}</a>
<br />${RESULT}";

done

echo "List of images:<br /><br />${RESULT}" > index.html

cd ../../source_git