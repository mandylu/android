#! /bin/sh
javaps=`jps -l |grep com.quanleimu.screenshot/screenshot.jar`;
if [ -n "$javaps" ]; then
	psid=`echo $javaps | awk '{print $1}'`;
	kill -9 $psid;
fi
echo "Start java screenshot jar"; 
java -jar ../com.quanleimu.screenshot/screenshot.jar &
echo "Started java screenshot jar";
sleep 1;
