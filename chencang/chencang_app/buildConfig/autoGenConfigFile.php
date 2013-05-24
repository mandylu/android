<?php
//penghui@baixing.com

$config = array(
	array(
		"category" => "ershou",
		"city"     => "all",
		"package"  => "com.cc.ershou",
		"name"     => "闲置物品",
		"umeng"    => "516f5f6256240b6a33019bdd",
	),
	array(
		"category" => "gongzuo",
		"city"     => "all",
		"package"  => "com.cc.quanzhizhaopin",
		"name"     => "招工信息大全",
		"umeng"    => "516f5f9d56240b6a2a018909",
	),
	array(
		"category" => "cheliang",
		"city"     => "all",
		"package"  => "com.cc.ershouche",
		"name"     => "便宜二手车",
		"umeng"    => "516f5fbf56240b6a3401b97c",
	),

	array(
		"category" => "zhengzu",
		"city"     => "all",
		"package"  => "com.cc.zufang",
		"name"     => "个人房屋出租",
		"umeng"    => "516f5fd356240b6b9501a87b",
	),
	array(
		"category" => "shouji",
		"city"     => "all",
		"package"  => "com.cc.shouji",
		"name"     => "手机每月换",
		"umeng"    => "516f5ff456240b6b9501a890",
	),
	array(
		"category" => "baomu",
		"city"     => "all",
		"package"  => "com.cc.baomu",
		"name"     => "保姆钟点工",
		"umeng"    => "516f600a56240b6a33019d3d",
	),

	array(
		"category" => "all",
		"city"     => "shanghai",
		"package"  => "com.cc.shanghai",
		"name"     => "上海生活信息",
		"umeng"    => "516f602056240b09ed00ca10",
	),
	array(
		"category" => "all",
		"city"     => "guangzhou",
		"package"  => "com.cc.guangzhou",
		"name"     => "广州生活信息",
		"umeng"    => "516f603a56240b6b9601c8fa",
	),
	array(
		"category" => "all",
		"city"     => "beijing",
		"package"  => "com.cc.beijing",
		"name"     => "北京生活信息",
		"umeng"    => "516f605d56240b689a016de3",
	),

	array(
		"category" => "ershou",
		"city"     => "shanghai",
		"package"  => "com.cc.shanghaiershou",
		"name"     => "上海闲置物品",
		"umeng"    => "516f607c56240b93e101d188",
	),
	array(
		"category" => "gongzuo",
		"city"     => "guangzhou",
		"package"  => "com.cc.guangzhouquanzhizhaopin",
		"name"     => "广州招工信息",
		"umeng"    => "516f608e56240b6a33019e30",
	),
	array(
		"category" => "cheliang",
		"city"     => "beijing",
		"package"  => "com.cc.beijingershouche",
		"name"     => "北京二手车",
		"umeng"    => "516f60a156240b6b9501a99b",
	),

	array(
		"category" => "zhengzu",
		"city"     => "shanghai",
		"package"  => "com.cc.shanghaizufang",
		"name"     => "上海个人租房",
		"umeng"    => "516f60b856240b6a1301ab58",
	),
	array(
		"category" => "shouji",
		"city"     => "guangzhou",
		"package"  => "com.cc.guangzhoushouji",
		"name"     => "广州二手手机",
		"umeng"    => "516f60cc56240b6a3201ad9d",
	),
	array(
		"category" => "baomu",
		"city"     => "beijing",
		"package"  => "com.cc.beijingbaomu",
		"name"     => "北京找保姆",
		"umeng"    => "516f60e856240b9b9c01f3e5",
	),
);


$template = file_get_contents(dirname(__FILE__) . "/AndroidManifest.template");
foreach($config as $gridConfig) {
	$filename = dirname(__FILE__) . DIRECTORY_SEPARATOR . $gridConfig["package"] . DIRECTORY_SEPARATOR. "AndroidManifest.xml";
	mkdir(dirname($filename), 0755);
	file_put_contents($filename, strtr($template, array(
		"{category}" => $gridConfig["category"],
		"{city}"     => $gridConfig["city"],
		"{package}"  => $gridConfig["package"],
		"{name}"     => $gridConfig["name"],
		"{umeng}"    => $gridConfig["umeng"],
	)));
}

?>