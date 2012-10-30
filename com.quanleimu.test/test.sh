#! /bin/sh

echo "START shell"

#adb -s emulator-5556 emu kill;
#adb -s emulator-5580 emu kill;
#adb kill-server;
#adb start-server;

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

LOGNOW=$(date +"S%Y%m%d");

cd baixing_quanleimu/;
ant clean;
ant release;
sleep 1;

LOGPATH="logs/test";

if [ ! -f bin/Baixing_QuanLeiMu-release.apk ];
then
	echo "ANT build baixing app error";
	echo "ANT build baixing app error" >> $LOGPATH/runadlisting_$LOGNOW.log
	echo "ANT build baixing app error" >> $LOGPATH/runpost_$LOGNOW.log
	echo "ANT build baixing app error" >> $LOGPATH/runpost2_$LOGNOW.log
	exit;
fi;

cd ../com.quanleimu.test/;
ant clean;
ant release;
sleep 1;

if [ ! -f bin/com.quanleimu.test-release.apk ];
then
	echo "ANT build baixing test app error";
	echo "ANT build baixing test app error" >> $LOGPATH/runadlisting_$LOGNOW.log
	echo "ANT build baixing test app error" >> $LOGPATH/runpost_$LOGNOW.log
	echo "ANT build baixing test app error" >> $LOGPATH/runpost2_$LOGNOW.log
	exit;
fi;
cd ../

#start testemulator1
echo "Check emulator-5556 device is connected or wait for one";
adbState=`adb -s emulator-5556 get-state`
if [ ! $adbState = "device" ]; then
	echo "Device emulator-5556 not found -- connect one to continue..."
	emulator -avd testemulator1 -port 5556 -sdcard com.quanleimu.test/testcase1.img &
	adb -s emulator-5556 wait-for-device
	echo "Device emulator-5556 connected."
	sleep 5;
fi

echo "reinstall pkg";
adb -s emulator-5556 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-5556 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-5556 uninstall com.quanleimu.activity;
adb -s emulator-5556 install -r baixing_quanleimu/bin/Baixing_QuanLeiMu-release.apk;
adb -s emulator-5556 install -r com.quanleimu.test/bin/com.quanleimu.test-release.apk ;

adb -s emulator-5556 shell logcat -d >> $LOGPATH/logcat_adlisting_$LOGNOW.log;
adb -s emulator-5556 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-5556 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-5556 shell logcat -c
#end testemulator1 
sleep 3;

#start testemulator2
echo "Check emulator-5580 device is connected or wait for one";
adbState=`adb -s emulator-5580 get-state`
if [ ! $adbState = "device" ]; then
	echo "Device emulator-5580 not found -- connect one to continue..."
	emulator -avd testemulator2 -port 5580 -sdcard com.quanleimu.test/testcase2.img &
	adb -s emulator-5580 wait-for-device
	echo "Device emulator-5580 connected."
	sleep 5;
fi

echo "reinstall pkg";
adb -s emulator-5580 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-5580 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-5580 uninstall com.quanleimu.activity;
adb -s emulator-5580 install -r baixing_quanleimu/bin/Baixing_QuanLeiMu-release.apk;
adb -s emulator-5580 install -r com.quanleimu.test/bin/com.quanleimu.test-release.apk ;

adb -s emulator-5580 shell logcat -d >> $LOGPATH/logcat_post_$LOGNOW.log;
adb -s emulator-5580 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-5580 emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-5580 shell logcat -c
#end testemulator2

sleep 5;

javaps=`jps -l |grep com.quanleimu.screenshot/screenshot.jar`;
if [ -n "$javaps" ]; then
	psid=`echo $javaps | awk '{print $1}'`;
	kill -9 $psid;
fi
echo "Start java screenshot jar"; 
java -jar com.quanleimu.screenshot/screenshot.jar &
echo "Started java screenshot jar";
sleep 1;

#start realdevice
adbState=`adb -s 015d18844854041c get-state`;
if [ ! $adbState = "device" ]; then
	echo "Device 015d18844854041c not found..."
else
	echo "Device 015d18844854041c connected."

	echo "reinstall pkg";	
	adb -s 015d18844854041c shell input keyevent 82
	adb -s 015d18844854041c shell input keyevent 4
	#adb -s 015d18844854041c emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
	#adb -s 015d18844854041c emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
	#adb -s 015d18844854041c uninstall com.quanleimu.activity;
	#adb -s 015d18844854041c install -r baixing_quanleimu/bin/Baixing_QuanLeiMu-release.apk;
	#adb -s 015d18844854041c install -r com.quanleimu.test/bin/com.quanleimu.test-release.apk ;
	
	adb -s 015d18844854041c shell input keyevent 82
	adb -s 015d18844854041c shell input keyevent 4
	#adb -s 015d18844854041c emu event send EV_KEY:KEY_MENU:1 EV_KEY:KEY_MENU:0;
	#adb -s 015d18844854041c emu event send EV_KEY:KEY_MENU:1 EV_KEY:KEY_MENU:0;
	#adb -s 015d18844854041c emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
	#adb -s 015d18844854041c emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
	
	adb -s 015d18844854041c shell logcat -d >> $LOGPATH/logcat_post2_$LOGNOW.log;
	adb -s 015d18844854041c shell logcat -c
	sleep 1;
	
	
	#test in real device
	echo "START test post2 $NOW" >> $LOGPATH/runpost2_$LOGNOW.log
	adb -s 015d18844854041c shell am instrument -w -e class com.quanleimu.activity.test.KeepLiveTest#runPost com.quanleimu.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner >> $LOGPATH/runpost2_$LOGNOW.log &
		
fi
#end realdevice

NOW=$(date +"%m-%d-%Y %H-%M");
#test adListing loop case
echo "START test adlisting $NOW" >> $LOGPATH/runadlisting_$LOGNOW.log
adb -s emulator-5556 shell am instrument -w -e class com.quanleimu.activity.test.KeepLiveTest#runAdListing com.quanleimu.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner >> $LOGPATH/runadlisting_$LOGNOW.log &

sleep 4;

#test post loop case
echo "START test post $NOW" >> $LOGPATH/runpost_$LOGNOW.log
adb -s emulator-5580 shell am instrument -w -e class com.quanleimu.activity.test.KeepLiveTest#runPost com.quanleimu.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner >> $LOGPATH/runpost_$LOGNOW.log &

#test all case
#adb shell am instrument -w -e class com.quanleimu.activity.test com.quanleimu.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner >> $LOGPATH/all_$LOGNOW.log ;

echo "STOP shell"