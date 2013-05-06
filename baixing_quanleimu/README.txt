－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－
＃Build 相关
＃＃相关文件
	＃build_rel.xml ——ant 执行脚本
	ant -buildfile build_rel.xml release
	
	#local.properties ——build_rel.xml 引用
	用来放本地（build machine）环境变量的配置,每个build machine上的环境变量不同而不同
	
	＃ant.properties ——build_rel.xml 引用
	用来存放打包相关参数，如默认chanel list列表,key store的路径和访问key store的密码
	
	#project.properties ——工程相关的描述，编译脚本和IDE（eclipse）会使用该文件
	当前project引用的子工程描述
	android.library.reference.1=../library/com.baixing.network
	
＃＃特别说明
     AndroidManifest.xml里修改和重构的时候注意不要修改的两行信息的格式，因为这个涉及到build时的文本替换。
     
      <!-- Warning ::: do not formator following two line.-->
        <meta-data android:name="UMENG_CHANNEL" android:value="androidmarket_umeng" />
        <meta-data android:name="publishTime" android:value="2012-09-29 11:05" /> 

－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－


	