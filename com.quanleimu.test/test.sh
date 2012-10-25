#! /bin/sh

adb -s emulator-5581 emu kill;
adb -s emulator-5580 emu kill;

(echo "")|android create avd -n testemulator1 -t 2 -c com.quanleimu.test/testcase1.img;
(echo "")|android create avd -n testemulator1 -t 2 -c com.quanleimu.test/testcase2.img;

emulator -avd testemulator1 -port 5580 -sdcard com.quanleimu.test/testcase1.img &
sleep 1;
emulator -avd testemulator2 -port 5581 -sdcard com.quanleimu.test/testcase2.img &

adb -s emulator-5580 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-5580 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-5581 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-5581 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;

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


adb -s emulator-5580 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-5580 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-5581 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-5581 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;

sleep 2;

#test adListing,post loop case
adb -s emulator-5580 shell am instrument -w -e class com.quanleimu.activity.test.KeepLiveTest#runAdListing com.quanleimu.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner >> testcaserunadlisting.log &;
adb -s emulator-5581 shell am instrument -w -e class com.quanleimu.activity.test.KeepLiveTest#runPost com.quanleimu.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner >> testcaserunpost.log &;

#test all case
#adb shell am instrument -w -e class com.quanleimu.activity.test com.quanleimu.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner >> testcaseall.log ;
