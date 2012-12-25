<?php
//print_r($argv);
if (count($argv) < 2) {
	echo "请输入代码目录\n";
	exit;
}

$obj = new CaculateFiles();

$obj->maxLine = 500;
if (count($argv) > 2) {
	$obj->ext = $argv[2];
} else {
	$obj->ext = ".java";
}
if (count($argv) > 3) {
	$obj->lineSkip = explode(",", $argv[3]);
} else {
	$obj->lineSkip = array("//");
}
$obj->run($argv[1]);

class CaculateFiles {
    /**
     * 统计的后缀
     */
    public $ext = ".php";
    /**
     * 是否显示每个文件的统计数
     */
    private $showEveryFile = true;
    /**
     * 文件的的跳过规则
     */
    private $fileSkip = array();
    /**
     * 统计的跳过行规则
     */
    public $lineSkip = array("//", "#"); //for java array("//");
	private $blockSkip = array("/*", "*/");
    /**
     * 统计跳过的目录规则
     */
    private $dirSkip = array(".", "..", '.svn');
	
	private static $BLANK_LINE = 1;
	private static $COMMENT_LINE = 2;
	private static $CODE_LINE = 3;
	private static $COMMENT_BLOCK_LINE = 4;//注释行，但前面有代码
	private static $COMMENT_BLOCK_END_LINE = 5;//注释行结束，后面有代码
	private static $COMMENT_BLOCK_START_LINE = 6;//注释行开始。前面无代码
	private static $COMMENT_BLOCK_ENDED_LINE = 7;//注释行结束。后面无代码
	
    private $lines = array('totalLine' => 0, 'blankNum' => 0, 'commentNum' => 0, 'fileNum' => 0);
	private $owner = array(
			//'ownername' => array('totalLine' => 0, 'blankNum' => 0, 'commentNum' => 0, 'fileNum' => 0),
		);
		//file lines > 1000
	private	$ownerfiles = array(
			//'ownername' => array('filename' => 'name', 'lineNum' => 0, 'blankNum' => 0, 'commentNum' => 0),
		);
	
	public $maxLine = 1000;
   
    public function __construct($ext = '', $dir = '', $showEveryFile = true, $dirSkip = array(), $lineSkip = array(), $fileSkip = array()) {
        $this->setExt($ext);
        $this->setDirSkip($dirSkip);
        $this->setFileSkip($fileSkip);
        $this->setLineSkip($lineSkip);
        $this->setShowFlag($showEveryFile);
        $this->run($dir);
    }
   
    public function setExt($ext) {
        trim($ext) && $this->ext = strtolower(trim($ext));
    }
    public function setShowFlag($flag = true) {
        $this->showEveryFile = $flag;
    }
    public function setDirSkip($dirSkip) {
        $dirSkip && is_array($dirSkip) && $this->dirSkip = $dirSkip;
    }
    public function setFileSkip($fileSkip) {
        $this->fileSkip = $fileSkip;
    }
    public function setLineSkip($lineSkip) {
        $lineSkip && is_array($lineSkip) && $this->lineSkip = array_merge($this->lineSkip, $lineSkip);
    }
    /**
     * 执行统计
     * @param string $dir 统计的目录
     */
    public function run($dir = '') {
        if ($dir == '') return;
		$this->read($dir);
		$this->printTable();
		
    }
	
	private function read($path) {
		if(is_dir($path)){
			$dp=dir($path);
			while($file=$dp->read())
				if($file != '.'&&$file!='..')
					$this->read($path . '/' . $file);
			$dp->close();
        }
		if ($this->skipFile($path)) return;
        $own = $this->getOwner($path);
		list($num1, $num2, $num3) = $this->readfiles($path);
		$this->lines['totalLine'] += $num1;
		$this->lines['blankNum'] += $num2;
		$this->lines['commentNum'] += $num3;
		$this->lines['fileNum']++;
		if (!isset($this->owner[$own])) {
			$this->owner[$own] = array('totalLine' => 0, 'blankNum' => 0, 'commentNum' => 0, 'fileNum' => 0);
		}
		$this->owner[$own]['totalLine'] += $num1;
		$this->owner[$own]['blankNum'] += $num2;
		$this->owner[$own]['commentNum'] += $num3;
		$this->owner[$own]['fileNum']++;
		if ($num1 > $this->maxLine) {
			if (!isset($this->ownerfiles[$own])) {
				$this->ownerfiles[$own] = array();
			}
			$fileLine = array(
				'totalLine' => $num1,
				'blankNum' => $num2,
				'commentNum' => $num3
			);
			$this->ownerfiles[$own][substr ($path, strrpos ($path, "/") + 1)] = $fileLine;
		} 
	}
	
	private function printTable() {
		$width = 600;
		echo "\\<table width='{$width}' border='1' cellspacing='0' \\>";
		echo "\\<tr bgcolor='#88aa99'\\>\\<td\\>total\\<\\/td\\>\\<td\\>blank\\<\\/td\\>\\<td\\>comment\\<\\/td\\>\\<td\\>files\\<\\/td\\>\\<\\/tr\\>";
		echo "\\<tr\\>\\<td\\>{$this->lines['totalLine']}\\<\\/td\\>";
		echo "\\<td\\>{$this->lines['blankNum']}\\<\\/td\\>";
		echo "\\<td\\>{$this->lines['commentNum']}\\<\\/td\\>";
		echo "\\<td\\>{$this->lines['fileNum']}\\<\\/td\\>\\<\\/tr\\>";
		echo "\\<\\/table\\>\\<br \\/\\>\\<br \\/\\>";
		$tmpOwner = array();
		foreach ($this->owner as $name => $line) {
			$tmpOwner[$name] = $line['totalLine'];
		}
		arsort($tmpOwner);
		echo "\\<table width='{$width}' border='1' cellspacing='0' \\>";
		echo "\\<tr bgcolor='#88aa99'\\>\\<td\\>name\\<\\/td\\>\\<td\\>total\\<\\/td\\>\\<td\\>blank\\<\\/td\\>\\<td\\>comment\\<\\/td\\>\\<td\\>files\\<\\/td\\>\\<\\/tr\\>";
		foreach ($tmpOwner as $name => $tl) {
			$line = $this->owner[$name];
			echo "\\<tr\\>\\<td\\>{$name}\\<\\/td\\>";
			echo "\\<td\\>{$line['totalLine']}\\<\\/td\\>";
			echo "\\<td\\>{$line['blankNum']}\\<\\/td\\>";
			echo "\\<td\\>{$line['commentNum']}\\<\\/td\\>";
			echo "\\<td\\>{$line['fileNum']}\\<\\/td\\>\\<\\/tr\\>";
		}
		echo "\\<\\/table\\>\\<br \\/\\>\\<br \\/\\>";
		echo "\\<table width='{$width}' border='1' cellspacing='0' \\>";
		echo "\\<tr bgcolor='#88aa99'\\>\\<td\\>name\\<\\/td\\>\\<td\\>file\\<\\/td\\>\\<td\\>total\\<\\/td\\>\\<td\\>blank\\<\\/td\\>\\<td\\>comment\\<\\/td\\>\\<\\/tr\\>";
		foreach ($tmpOwner as $name => $tl) {
			if (!isset($this->ownerfiles[$name])) continue;
			echo "\\<tr bgcolor='#88aa99'\\>\\<td\\>{$name}\\<\\/td\\>\\<td\\>\\<\\/td\\>\\<td\\>\\<\\/td\\>\\<td\\>\\<\\/td\\>\\<td\\>\\<\\/td\\>\\<\\/tr\\>";
			$tof = array();
			$ownfile = $this->ownerfiles[$name];
			foreach ($ownfile as $fn => $fl) {
				$tof[$fn] = $fl['totalLine'];
			}
			arsort($tof);
			foreach ($tof as $fn => $v) {
				$fl = $ownfile[$fn];
				echo "\\<tr\\>\\<td\\>\\<\\/td\\>";
				echo "\\<td\\>{$fn}\\<\\/td\\>";
				echo "\\<td\\>{$fl['totalLine']}\\<\\/td\\>";
				echo "\\<td\\>{$fl['blankNum']}\\<\\/td\\>";
				echo "\\<td\\>{$fl['commentNum']}\\<\\/td\\>";
				echo "\\<\\/tr\\>";
			}
		}
		echo "\\<\\/table\\>\\<br \\/\\>\\<br \\/\\>";
	}
 
    /**
     * 读取文件
     * @param string $file 文件
     */
    private function readfiles($file) {
        $str = file($file);
		$blankNum = 0;
		$commentNum = 0;
		$lineStatus = self::$BLANK_LINE;
        foreach ($str as $value) {
			if ($lineStatus == self::$COMMENT_BLOCK_START_LINE) {
				$numType = $this->checkBlockCommentEndLine($value);
				if ($numType == self::$COMMENT_BLOCK_END_LINE) {
					$numType = self::$BLANK_LINE;
				} else if ($numType == self::$COMMENT_BLOCK_ENDED_LINE) {
					$commentNum++;
					$numType = self::$BLANK_LINE;
				} else {
					$commentNum++;
				}
				continue;
			}
			$numType = $this->checkLine(trim($value));
            if ($numType == self::$BLANK_LINE) {
				$blankNum++;
			} else if ($numType == self::$COMMENT_LINE) {
				$commentNum++;
			} else if ($numType == self::$COMMENT_BLOCK_START_LINE) {
				$commentNum++;
				$lineStatus = self::$COMMENT_BLOCK_START_LINE;
			} else if ($numType == self::$COMMENT_BLOCK_LINE) {
				$lineStatus = self::$COMMENT_BLOCK_START_LINE;
			}
        }
        $totalnum = count(file($file));
        return array($totalnum, $blankNum, $commentNum);
    }
	
	function getOwner($path) {
		$lines = file($path);
		if (!empty($lines)) {
			$line = $lines[0];
			if (($s = strpos($line, "@baixing.com")) !== false) {
				return substr($line, 2, $s - 2);
			}
		}
		return "无主";
	}
    
    /**
     * 执行跳过的目录规则
     * @param string $dir 目录名
     */
    private function skipDir($dir) {
        if (in_array($dir, $this->dirSkip)) return true;
        return false;
    }
     
    /**
     * 执行跳过的文件规则
     * @param string $file 文件名
     */
    private function skipFile($file) {
        if (strtolower(strrchr($file, '.')) != $this->ext) return true;
        if (!$this->fileSkip) return false;
        foreach ($this->fileSkip as $skip) {
            if (strpos($file, $skip) === 0) return true;
        }
        return false;
    }
     
    /**
     * 执行文件中行的跳过规则
     * @param string $string 行内容
     */
    private function checkLine($string) {
        if ($string == '') return self::$BLANK_LINE;
		if (strpos($string, $this->blockSkip[0]) === 0) return self::$COMMENT_BLOCK_START_LINE;
		if (strpos($string, $this->blockSkip[0]) > 0) {
			foreach ($this->lineSkip as $tag) {
				if (strpos($string, $tag) === 0) return self::$COMMENT_LINE;
			}
			return self::$COMMENT_BLOCK_LINE;
		}
        foreach ($this->lineSkip as $tag) {
            if (strpos($string, $tag) === 0) return self::$COMMENT_LINE;
        }
        return self::$CODE_LINE;
    }
	private function checkBlockCommentEndLine($string) {
        if ($string == '') return self::$COMMENT_BLOCK_LINE;
		$start = strpos($string, $this->blockSkip[1]);
		if ($start >= 0) {
			$str2 = substr($string, $start + strlen($this->blockSkip[1]));
			if ($str2 == '') 
				return self::$COMMENT_BLOCK_ENDED_LINE;
			else {
				foreach ($this->lineSkip as $tag) {
					if (strpos($str2, $tag) === 0) return self::$COMMENT_BLOCK_ENDED_LINE;
				}	
				return self::$COMMENT_BLOCK_END_LINE;
			}
		}
		return self::$COMMENT_BLOCK_LINE;
    }
}
