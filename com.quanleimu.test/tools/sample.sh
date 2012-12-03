#! /bin/sh

#adb shell am instrument -w -e class com.baixing.activity.test.MainActivityTest com.baixing.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner
adb shell am instrument -w -e class com.baixing.activity.test.MainActivityTest#testSearchClick com.baixing.activity.test/pl.polidea.instrumentation.PolideaInstrumentationTestRunner