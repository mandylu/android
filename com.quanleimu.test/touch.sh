echo $1_$2_$3
adb -s $1 shell sendevent /dev/input/event0 3 0 $2
adb -s $1 shell sendevent /dev/input/event0 3 1 $3 
adb -s $1 shell sendevent /dev/input/event0 1 330 1 
adb -s $1 shell sendevent /dev/input/event0 0 0 0 
adb -s $1 shell sendevent /dev/input/event0 1 330 0 
adb -s $1 shell sendevent /dev/input/event0 0 0 0

#情况2:模拟滑动轨迹（可下载并采用aPaint软件进行试验）
#如下例是在aPaint软件上画出一条开始于（100,200），止于（108,200）的水平直线
#adb shell sendevent /dev/input/event0 3 0 100 //start from point (100,200)
#adb shell sendevent /dev/input/event0 3 1 200
#adb shell sendevent /dev/input/event0 1 330 1 //touch
#adb shell sendevent /dev/input/event0 0 0 0
#adb shell sendevent /dev/input/event0 3 0 101 //step to point (101,200)
#adb shell sendevent /dev/input/event0 0 0 0
#adb shell sendevent /dev/input/event0 3 0 108 //end point(108,200)
#adb shell sendevent /dev/input/event0 0 0 0
