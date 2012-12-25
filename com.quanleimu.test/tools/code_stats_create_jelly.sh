#! /bin/sh
echo $LANG
DIR="$(cd "$(dirname "$0")" && pwd)";
newcontent="`php ${DIR}/code_stats.php $1`";

if [ -z ${JENKINS_HOME} ];then
	JENKINS_HOME="${HOME}/.jenkins";
fi

sed "s/MAIL_CONTENT/${newcontent}/g" ${JENKINS_HOME}/email-templates/content.jelly.template > ${JENKINS_HOME}/email-templates/content.jelly
