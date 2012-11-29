#! /bin/sh
# test.sh auto run android TestCase

LOGNOW=$(date +"S%Y%m%d");
LOGPATH="logs/test";
DIR="$(cd "$(dirname "$0")" && pwd)";

cd $DIR;
cd ../../
mkdir -p "logs";
mkdir -p $LOGPATH;
mkdir -p "logs/screen";
mkdir -p "logs/logcat";
mkdir -p "logs/emulator/";
chmod -R 777 "logs";

# define create_emulator function
create_emulator() {
	local emulator="$1"
	local type="$2"
	if [ $type = "" ]; then
		type="android-8"
	fi
	typeid=`android list |grep "$type"|awk '{print $2}'`;
	if [ "$typeid" = "" ];then
		echo "create emulator error by $type";
		exit 1;
	fi
	(echo "")|android create avd -n $emulator -t $typeid -s WVGA800;
	if [ ! -f "logs/emulator/$emulator.img" ];
	then
		mksdcard 256M "logs/emulator/$emulator.img"
	fi
}

# define start_emulator function
start_emulator() {
	local emulator="$1"
	local port="$2"
	echo "Check emulator-$port device is connected or wait for one";
	adbState=`adb -s emulator-$port get-state | grep device`;
	if [ "$adbState" = "device" ]; then
	    echo "started"
	else
		echo "Device emulator-$port not found -- connect one to continue..."
		emulator -avd $emulator -port $port -sdcard logs/emulator/$emulator.img &
		adb -s emulator-$port wait-for-device
		echo "Device emulator-$port connected."
		sleep 30;
	fi
}

# define install_pkg function
install_pkg() {
	local port="$1"
	adb -s emulator-$port emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
	adb -s emulator-$port emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
	adb -s emulator-$port uninstall com.quanleimu.activity;
	adb -s emulator-$port install -r baixing_quanleimu/bin/Baixing_QuanLeiMu-release.apk;
	adb -s emulator-$port install -r com.quanleimu.test/bin/com.quanleimu.test-release.apk ;
	
	adb -s emulator-$port shell logcat -d >> $LOGPATH/build_$LOGNOW.log;
	adb -s emulator-$port emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
	adb -s emulator-$port emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
	adb -s emulator-$port shell logcat -c
	sleep 3;
}

# define start_screen function
start_screen() {
	local javaps=`jps -l |grep com.quanleimu.screenshot/screenshot.jar`;
	if [ -n "$javaps" ]; then
		local psid=`echo $javaps | awk '{print $1}'`;
		kill -9 $psid;
	fi
	echo "Start java screenshot jar"; 
	java -jar com.quanleimu.screenshot/screenshot.jar &
	echo "Started java screenshot jar";
	sleep 1;
}

# define start_test function
run_test() {
	NOW=$(date +"%m-%d-%Y %H-%M");
	local emulator="$1"
	local func="$2"
	local prefix="$3"
	
	echo "START test $func $NOW" >> $LOGPATH/"$prefix"_$LOGNOW.log
	adb -s $emulator shell am instrument -w -e class $func com.quanleimu.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner >> $LOGPATH/"$prefix"_$LOGNOW.log &

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
		adb -s $emulator install -r com.quanleimu.test/bin/com.quanleimu.test-release.apk ;
		
		adb -s $emulator shell input keyevent 82
		adb -s $emulator shell input keyevent 4
		
		adb -s $emulator shell logcat -d >> $LOGPATH/build_$LOGNOW.log;
		adb -s $emulator shell logcat -c
		sleep 1;
		
		REALDEVICE="1"
		
	else
		REALDEVICE="0"
		echo "Device $emulator not found..."
	fi
}

# define build_pkg function
build_pkg() {
	cd $DIR;
	cd ../../;
	svn up;
	
	if [ ! -f "../../buildconfig/local.properties" ];then
		cp -f com.quanleimu.test/local.properties baixing_quanleimu/local.properties;
	else
		cp "../../buildconfig/local.properties" baixing_quanleimu/local.properties;
	fi
	
	cd baixing_quanleimu/;
	ant clean;
	ant release;
	sleep 1;
	
	if [ ! -f bin/Baixing_QuanLeiMu-release.apk ];
	then
		echo "ANT build baixing app error";
		echo "ANT build baixing app error" >> $LOGPATH/build_$LOGNOW.log
		exit;
	fi;
	
	cd ../../
	
	if [ -f "../../buildconfig/local.properties" ];then
		cp "../../buildconfig/local.properties" com.quanleimu.test/local.properties;
	fi
	
	cd com.quanleimu.test/;
	ant clean;
	ant release;
	sleep 1;
	
	if [ ! -f bin/com.quanleimu.test-release.apk ];
	then
		echo "ANT build baixing test app error";
		echo "ANT build baixing test app error" >> $LOGPATH/build_$LOGNOW.log
		exit;
	fi;
	cd ../
	
	cd com.quanleimu.screenshot/;
	ant
	sleep 1;
	cd ../
}

echo "START test"

#adb -s emulator-5556 emu kill;
#adb -s emulator-5580 emu kill;
adb kill-server;
adb start-server;

#create_emulator testemulator1 "android-8";  #cmd "android list" get 2.2 id = android-8
#create_emulator testemulator2 "android-8";
#create_emulator testemulator3 "android-16"; #cmd "android list" get 4.1.2 id = android-16
#create_emulator testemulator4 "android-16";

build_pkg;

#start_emulator "testemulator1" "5556"
#install_pkg "5556"

#start_emulator "testemulator2" "5580"
#install_pkg "5580"

start_emulator "testemulator3" "5558"
install_pkg "5558"

#start_emulator "testemulator4" "5560"
#install_pkg "5560"

start_screen

#run_test "emulator-5556" "com.quanleimu.activity.test.KeepLiveTest#runAdListing" "run_adlisting"
#run_test "emulator-5580" "com.quanleimu.activity.test.KeepLiveTest#runPost" "run_post"
run_test "emulator-5558" "com.quanleimu.activity.test.BXTestSuite" "run_test"
#run_test "emulator-5560" "com.quanleimu.activity.test" "run_test"


start_real_device "015d18844854041c"
if [ "$REALDEVICE" = "1" ]; then
	echo "015d18844854041c test if connected";
	#run_test "015d18844854041c" "com.quanleimu.activity.test.KeepLiveTest#runPostAll" "run_post_all"
fi
start_real_device "015d1458a51c0c0e"
if [ "$REALDEVICE" = "1" ]; then
	echo "015d1458a51c0c0e test if connected";
	run_test "015d1458a51c0c0e" "com.quanleimu.activity.test.KeepLiveTest#runAdListing" "run_adListing"
fi

echo "END"