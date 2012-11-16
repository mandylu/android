#! /bin/sh
usage(){
	echo "Usage: $0 port"
	exit 1
}
[[ $# -lt 1 ]] && usage
port="$1"
adb -s emulator-$port emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-$port emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-$port uninstall com.quanleimu.activity;
adb -s emulator-$port install -r ./bin/android_baixing_baixing.apk;
adb -s emulator-$port install -r ../com.quanleimu.test/bin/com.quanleimu.test-release.apk ;

adb -s emulator-$port emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;
adb -s emulator-$port emu event send EV_KEY:KEY_SOFT1:1 EV_KEY:KEY_SOFT1:0;

sleep 3;
