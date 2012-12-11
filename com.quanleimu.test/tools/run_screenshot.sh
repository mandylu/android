#! /bin/sh
javaps=`jps -l |grep com.quanleimu.screenshot/screenshot.jar`;
if [ -n "$javaps" ]; then
	psid=`echo $javaps | awk '{print $1}'`;
	kill -9 $psid;
fi
echo "Start java screenshot jar"; 
echo `pwd`;

if [ ! -f "com.quanleimu.screenshot/screenshot.jar" ];
then
	cd ../
	java -jar source_git/com.quanleimu.screenshot/screenshot.jar & 
	cd source_git/
else
	java -jar com.quanleimu.screenshot/screenshot.jar & 
fi
echo "Started java screenshot jar";
sleep 1;
