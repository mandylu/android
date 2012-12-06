#!/bin/sh
usage(){
	echo "Usage: $0 path extension"
	echo "sample: $0  ../logs/screen/ .png"
	exit 1
}
[[ $# -lt 2 ]] && usage

DIR="$(cd "$(dirname "$0")" && pwd)";

cd "$1";

fileExt="$2"

FILE_LIST="`ls *${fileExt}`"

RESULT=""
for file in ${FILE_LIST}
do
    RESULT="<a href=\"${file}\" target=\"_blank\">${file}</a>
<br />${RESULT}";

done

filetype="files";
if [ "$fileExt" = ".png" ]; then
	filetype="images";
fi
echo "List of ${filetype}:<br /><br />${RESULT}" > index.html

cd $DIR
