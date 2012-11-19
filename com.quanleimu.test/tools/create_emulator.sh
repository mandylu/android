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
typeid=`android list |grep "$type"|awk '{print $2}'`;
if [ "$typeid" = "" ];then
	echo "create emulator error by $type";
	exit 1;
fi
abi=""
if [ "$type" = "" ];then
	abi=" --abi armeabi-v7a"
fi
(echo "")|android create avd -n $emulator -t $typeid $abi -s WVGA800;
if [ ! -f "$imgpath/$emulator.img" ];
then
	mksdcard 256M "$imgpath/$emulator.img"
fi
