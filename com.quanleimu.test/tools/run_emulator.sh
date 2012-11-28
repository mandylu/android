#! /bin/sh
usage(){
	echo "Usage: $0 emulatorname port imgpath"
	exit 1
}
[[ $# -lt 3 ]] && usage

adb kill-server;
sleep 1;
adb start-server;
sleep 1;

emulator="$1"
port="$2"
imgpath="$3"

################################################
# reboots device from fastboot to adb or
# times out
# Globals:
#   device
#   ADB
# Arguments:
#   None
# Returns:
#   None
################################################
wait_for_boot_complete()
{
  echo "waiting for device to finish booting"
  local result=$(adb -s emulator-$port shell getprop dev.bootcomplete)
  local result_test=${result:1:1}
  echo -n "."
  while [ -z $result_test ]; do
    sleep 1
    echo -n "."
    result=$(adb -s emulator-$port shell getprop dev.bootcomplete)
    result_test=${result:0:1}
  done
  echo "finished booting"
}

echo "Check emulator-$port device is connected or wait for one";
adbState=`adb -s emulator-$port get-state | grep device`;
if [ "$adbState" = "device" ]; then
    echo "started"
else
	echo "Device emulator-$port not found -- connect one to continue..."
	echo "emulator -avd $emulator -port $port -sdcard $imgpath/$emulator.img"
	if [ -f "$imgpath/$emulator.img.lock" ];
	then
		rm "$imgpath/$emulator.img.lock";
	fi
	emulator -avd $emulator -port $port -sdcard $imgpath/$emulator.img &
	adb -s emulator-$port wait-for-device
	wait_for_boot_complete
	echo "Device emulator-$port connected."
	sleep 60;
fi
