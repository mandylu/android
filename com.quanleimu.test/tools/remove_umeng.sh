#! /bin/sh

cp baixing_quanleimu/src/com/baixing/activity/MainActivity.java MainActivity.java.bak
sed 's/UmengUpdateAgent\.update/\/\/UmengUpdateAgent\.update/' < baixing_quanleimu/src/com/baixing/activity/MainActivity.java > baixing_quanleimu/src/com/baixing/activity/MainActivity.java.2
mv baixing_quanleimu/src/com/baixing/activity/MainActivity.java.2 baixing_quanleimu/src/com/baixing/activity/MainActivity.java