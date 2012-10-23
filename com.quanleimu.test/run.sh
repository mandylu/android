#! /bin/sh

#adb shell am instrument -w -e class com.quanleimu.activity.test.MainActivityTest com.quanleimu.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner
adb shell am instrument -w -e class com.quanleimu.activity.test.MainActivityTest#testSearchClick com.quanleimu.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner