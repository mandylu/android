#! /bin/sh

cp baixing_quanleimu/src/com/quanleimu/activity/QuanleimuMainActivity.java QuanleimuMainActivity.java.bak
sed 's/UmengUpdateAgent\.update/\/\/UmengUpdateAgent\.update/' < baixing_quanleimu/src/com/quanleimu/activity/QuanleimuMainActivity.java > baixing_quanleimu/src/com/quanleimu/activity/QuanleimuMainActivity.java.2
mv baixing_quanleimu/src/com/quanleimu/activity/QuanleimuMainActivity.java.2 baixing_quanleimu/src/com/quanleimu/activity/QuanleimuMainActivity.java
