#! /bin/sh
usage(){
	echo "Usage: $0 port"
	exit 1
}
[[ $# -lt 1 ]] && usage
port="$1"
adb -s emulator-$port emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-$port emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
result=`adb -s emulator-$port uninstall com.quanleimu.activity`;
if [ "$result" == "Error: Could not access the Package Manager.  Is the system running?" ];then
	sleep 120;
	result=`adb -s emulator-$port uninstall com.quanleimu.activity`;
	if [ "$result" == "Error: Could not access the Package Manager.  Is the system running?" ];then sleep 120; fi
fi
adb -s emulator-$port install -r baixing_quanleimu/bin/android_baixing_baixing.apk;
adb -s emulator-$port install -r com.quanleimu.test/bin/com.quanleimu.test-release.apk ;

adb -s emulator-$port emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-$port emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;

sleep 3;
