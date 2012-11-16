#! /bin/sh
usage(){
	echo "Usage: $0 emulatorname type"
	exit 1
}
[[ $# -lt 2 ]] && usage

emulator="$1"
type="$2"
if [ $type = "" ]; then
	type="android-8"
fi
typeid=`android list |grep "android-16"|awk '{print $2}'`;
if [ "$typeid" = "" ];then
	echo "create emulator error by $type";
	exit 1;
fi
(echo "")|android create avd -n $emulator -t $typeid -s WVGA800;
if [ ! -f "logs/emulator/$emulator.img" ];
then
	mksdcard 256M "logs/emulator/$emulator.img"
fi
