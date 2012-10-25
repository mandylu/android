#! /bin/sh

DIR="$(cd "$(dirname "$0")" && pwd)";
cd $DIR;
cd ../;
svn up;

cp -f com.quanleimu.test/local.properties baixing_quanleimu/local.properties;

adb uninstall com.quanleimu.activity;
cd baixing_quanleimu/;
ant clean;
ant release;
adb install -r bin/Baixing_QuanLeiMu-release.apk;
cd ../  ;
cd com.quanleimu.test/;
ant clean;
ant release;
adb install -r bin/com.quanleimu.test-release.apk ;

#test adListing,post loop case
adb shell am instrument -w -e class com.quanleimu.activity.test.KeepLiveTest#runAdListing com.quanleimu.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner >> testcaserunadlisting.log ;
adb shell am instrument -w -e class com.quanleimu.activity.test.KeepLiveTest#runPost com.quanleimu.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner >> testcaserunpost.log ;

#test all case
#adb shell am instrument -w -e class com.quanleimu.activity.test com.quanleimu.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner >> testcaseall.log ;
