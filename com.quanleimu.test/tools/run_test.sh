#! /bin/sh
usage(){
	echo "Usage: $0 emulatorName class[#func]"
	exit 1
}
[[ $# -lt 2 ]] && usage
emulator="$1"
func="$2"
	
echo "START test $func "
adb -s $emulator shell am instrument -w -e class $func com.quanleimu.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner