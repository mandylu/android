#! /bin/sh
usage(){
	echo "Usage: $0 emulatorname port"
	exit 1
}
[[ $# -lt 2 ]] && usage
emulator="$1"
port="$2"
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
