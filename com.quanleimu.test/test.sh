#! /bin/sh

DIR="$(cd "$(dirname "$0")" && pwd)";
cd $DIR;
cd ../;
svn up;

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

adb shell am instrument -w -e class com.quanleimu.activity.test.KeepLiveTest#runAdListing com.quanleimu.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner >> testcaserunadlisting.log ;
adb shell am instrument -w -e class com.quanleimu.activity.test.KeepLiveTest#runPost com.quanleimu.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner >> testcaserunpost.log ;
