#! /bin/sh

echo "START shell"

(echo "")|android create avd -n testemulator1 -t 2 ;

sleep 2;

(echo "")|android create avd -n testemulator2 -t 2 ;

if [ ! -f com.quanleimu.test/testcase1.img ];
then
	mksdcard 256M com.quanleimu.test/testcase1.img
fi
if [ ! -f com.quanleimu.test/testcase1.img ];
then
	mksdcard 256M com.quanleimu.test/testcase2.img
fi

sleep 2;


DIR="$(cd "$(dirname "$0")" && pwd)";
cd $DIR;
cd ../;
svn up;

cp -f com.quanleimu.test/local.properties baixing_quanleimu/local.properties;

cd baixing_quanleimu/;
ant clean;
ant release;
cd ../com.quanleimu.test/;
ant clean;
ant release;
cd ../

#start testemulator1

#adb -s emulator-5556 emu kill;
emulator -avd testemulator1 -port 5556 -sdcard com.quanleimu.test/testcase1.img &

adb -s emulator-5556 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-5556 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-5556 uninstall com.quanleimu.activity;
adb -s emulator-5556 install -r baixing_quanleimu/bin/Baixing_QuanLeiMu-release.apk;
adb -s emulator-5556 install -r com.quanleimu.test/bin/com.quanleimu.test-release.apk ;

adb -s emulator-5556 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-5556 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;

sleep 2;
#end testemulator1 
sleep 3;

#start testemulator2
#adb -s emulator-5580 emu kill;
emulator -avd testemulator2 -port 5580 -sdcard com.quanleimu.test/testcase2.img &

adb -s emulator-5580 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-5580 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-5580 uninstall com.quanleimu.activity;
adb -s emulator-5580 install -r baixing_quanleimu/bin/Baixing_QuanLeiMu-release.apk;
adb -s emulator-5580 install  -r com.quanleimu.test/bin/com.quanleimu.test-release.apk ;

adb -s emulator-5580 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-5580 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;

#end testemulator2

#test adListing loop case
adb -s emulator-5556 shell am instrument -w -e class com.quanleimu.activity.test.KeepLiveTest#runAdListing com.quanleimu.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner >> testcaserunadlisting.log &

sleep 4;

#test post loop case
adb -s emulator-5580 shell am instrument -w -e class com.quanleimu.activity.test.KeepLiveTest#runPost com.quanleimu.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner >> testcaserunpost.log &


#test all case
#adb shell am instrument -w -e class com.quanleimu.activity.test com.quanleimu.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner >> testcaseall.log ;

echo "STOP shell"