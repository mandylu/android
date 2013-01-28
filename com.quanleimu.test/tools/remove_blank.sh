#! /bin/sh

cp baixing_quanleimu/res/values/strings.xml strings.xml.bak;
sed 's/ 百姓网/百姓网/' < baixing_quanleimu/res/values/strings.xml > baixing_quanleimu/res/values/strings.xml.2
mv baixing_quanleimu/res/values/strings.xml.2 baixing_quanleimu/res/values/strings.xml;
