#! /bin/sh
javaps=`jps -l |grep com.quanleimu.screenshot/screenshot.jar`;
if [ -n "$javaps" ]; then
	psid=`echo $javaps | awk '{print $1}'`;
	kill -9 $psid;
fi
echo "Start java screenshot jar"; 
cd ../../../
java -jar source/trunk/com.quanleimu.screenshot/screenshot.jar & 
echo "Started java screenshot jar";
sleep 1;
cd source/trunk/baixing_quanleimu/
