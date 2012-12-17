#! /bin/sh
# test.sh run one android TestCase

LOGNOW=$(date +"S%Y%m%d");
LOGPATH="logs/test";
DIR="$(cd "$(dirname "$0")" && pwd)";

# define usage function
usage(){
	echo "Usage: $0 (port|realDeviceName) \".class$function\" run_log [rebuild_baixing_pkg]"
	echo "	sample1: $0 5580 \".KeepLiveTest#runPost\" run_post"
	echo "	sample2: $0 5580 \".MainActivityTest\" run_main"
	echo "	sample3: $0 5580 \"\" run_all"
	echo "	sample4: $0 5580 \".KeepLiveTest#runPost\" run_post no"
	echo "	sample5: $0 015d18844854041c \".KeepLiveTest#runPost\" run_post"
	echo "  sample6: $0 015d1458a51c0c0e \".KeepLiveTest#runOnePost\" run_post"
	adb devices;
	exit 1
}

PORTDEVICE=$1;
CLASS=$2;
LOGNAME=$3;
REBUILD=$4;

adb kill-server;
sleep 1;
adb start-server;
sleep 1;

if [ "$CLASS" = "" ]; then
	CLASS=".BXTestSuite";
fi

# define install_pkg function
install_pkg() {
	local port="$1"
	adb -s emulator-$port emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
	adb -s emulator-$port emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
	adb -s emulator-$port uninstall com.quanleimu.activity;
	adb -s emulator-$port install -r baixing_quanleimu/bin/Baixing_QuanLeiMu-release.apk;
	adb -s emulator-$port install -r com.quanleimu.test/bin/com.baixing.test-release.apk ;
	
	adb -s emulator-$port shell logcat -d >> $LOGPATH/logcat_emulator-"$port"_$LOGNOW.log;
	adb -s emulator-$port emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
	adb -s emulator-$port emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
	adb -s emulator-$port shell logcat -c
	sleep 3;
}

# define start_test function
run_test() {
	NOW=$(date +"%m-%d-%Y %H-%M");
	local emulator="$1"
	local func="$2"
	local prefix="$3"
	echo "START test $func $NOW" >> $LOGPATH/"$prefix"_$LOGNOW.log
	
	sh com.quanleimu.test/tools/run_screenshot.sh >> $LOGPATH/${prefix}_${LOGNOW}_screen.log
	
	adb -s $emulator shell am instrument -w -e class $func com.baixing.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner >> $LOGPATH/"$prefix"_$LOGNOW.log &
	echo "adb -s $emulator shell am instrument -w -e class $func com.baixing.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner >> $LOGPATH/"$prefix"_$LOGNOW.log &"
	echo "tail -f $LOGPATH/"$prefix"_$LOGNOW.log"
	tail -f $LOGPATH/"$prefix"_$LOGNOW.log
	
	
}

# define build_pkg function
build_pkg() {
	cd $DIR;
	cd ../../;
	
	buildBaixing="$1";
	if [ ! -f baixing_quanleimu/bin/Baixing_QuanLeiMu-release.apk ]; then
		buildBaixing="";
	fi;
	
	if [ "$buildBaixing" = "" ]; then
		cp -f com.quanleimu.test/local.properties baixing_quanleimu/local.properties;
		
		cd baixing_quanleimu/;
		ant clean;
		ant release;
		sleep 1;
		
		if [ ! -f bin/Baixing_QuanLeiMu-release.apk ]; then
			echo "ANT build baixing app error";
			exit;
		fi;
		
		cd ../
		if [ ! -f com.quanleimu.screenshot/screenshot.jar ]; then
			cd com.quanleimu.screenshot/;
			ant
			sleep 1;
			cd ../com.quanleimu.test/;
		else
			cd com.quanleimu.test/;
		fi
		
	else
		cd com.quanleimu.test/;
	fi
	
	ant clean;
	ant release;
	sleep 1;
	
	if [ ! -f bin/com.baixing.test-release.apk ]; then
		echo "ANT build baixing test app error";
		exit;
	fi;
	cd ../
}

REALDEVICE="0"
# define start_real_device function
start_real_device() {
	local emulator="$1"
	adbState=`adb -s $emulator get-state | grep device`;
	if [ "$adbState" = "device" ]; then
	
		NOW=$(date +"%m-%d-%Y %H-%M");
		echo "Device $emulator connected."
	
		echo "reinstall pkg";
		adb -s $emulator shell input keyevent 82
		adb -s $emulator shell input keyevent 4
		adb -s $emulator uninstall com.quanleimu.activity;
		adb -s $emulator uninstall com.quanleimu.test;
		adb -s $emulator install -r baixing_quanleimu/bin/Baixing_QuanLeiMu-release.apk;
		adb -s $emulator install -r com.quanleimu.test/bin/com.baixing.test-release.apk ;
		
		adb -s $emulator shell input keyevent 82
		adb -s $emulator shell input keyevent 4
		
		adb -s $emulator shell logcat -d >> $LOGPATH/logcat_"$emulator"_$LOGNOW.log;
		adb -s $emulator shell logcat -c
		sleep 1;
		
		REALDEVICE="1"
		
	else
		REALDEVICE="0"
		echo "Device $emulator not found..."
	fi
}

is_real_device() {
	if grep '^[[:digit:]]*$' <<< "${PORTDEVICE}"; then
		if [ $PORTDEVICE -gt 10000 ]; then
			REALDEVICE="1"
		fi
	else
		REALDEVICE="1"
	fi
}

[[ $# -lt 3 ]] && usage

build_pkg "$REBUILD"

echo $LOGNAME;

is_real_device

if [ "$REALDEVICE" = "1" ]; then
	start_real_device "$PORTDEVICE"
	if [ "$REALDEVICE" = "1" ]; then
		run_test "$PORTDEVICE" "com.baixing.activity.test$CLASS" "$LOGNAME"
	else
		echo "device connected error"
	fi
else
	install_pkg "$PORTDEVICE"
	run_test "emulator-$PORTDEVICE" "com.baixing.activity.test$CLASS" "$LOGNAME"
fi

