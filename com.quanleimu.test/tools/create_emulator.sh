#! /bin/sh
usage(){
	echo "Usage: $0 emulatorname type imgpath"
	exit 1
}
[[ $# -lt 3 ]] && usage

emulator="$1"
type="$2"
imgpath="$3"
if [ "$type" = "" ]; then
	type="android-8"
fi
adbState=`adb -s $emulator get-state | grep device`;
if [ "$adbState" = "device" ]; then
    echo "$emulator started"
else
	android delete avd -n $emulator;  #TODO...run exists avd will crash
	typeid=`android list |grep "$type"|awk '{print $2}'`;
	if [ "$typeid" = "" ];then
		echo "create emulator error by $type";
		exit 1;
	fi
	abi=""
	if [ "$type" = "android-16" ];then
		abi=" --abi armeabi-v7a"
	fi
	echo "android create avd -n $emulator -t $typeid $abi -s WVGA800";
	(echo "")|android create avd -n $emulator -t $typeid $abi -s WVGA800;
	if [ ! -f "$imgpath/$emulator.img" ];
	then
		mksdcard 256M "$imgpath/$emulator.img"
	fi
fi

