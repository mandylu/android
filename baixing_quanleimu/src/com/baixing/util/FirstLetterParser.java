package com.baixing.util;

import org.apache.http.util.EncodingUtils;

public class FirstLetterParser {
	public static String GetFirstLetter(String str)
	{
		String t1;
		String val="";
		for (int i=0;i<str.length();i++)
		{
			t1= str.substring(i,1);
			
			if (Ascii(t1)>=Ascii("!") && Ascii(t1)<=Ascii("~"))
			        val+=t1;
			else if (Ascii(t1)>=Ascii("啊") && Ascii(t1)<Ascii("芭"))
			        val+="A";
			else if (Ascii(t1)>=Ascii("芭") && Ascii(t1)<Ascii("擦"))
			        val+="B";
			else if (Ascii(t1)>=Ascii("擦") && Ascii(t1)<Ascii("搭"))
			        val+="C";
			else if (Ascii(t1)>=Ascii("搭") && Ascii(t1)<Ascii("蛾"))
			        val+="D";
			else if (Ascii(t1)>=Ascii("蛾") && Ascii(t1)<Ascii("发"))
			        val+="E";
			else if (Ascii(t1)>=Ascii("发") && Ascii(t1)<Ascii("噶"))
			        val+="F";
			else if (Ascii(t1)>=Ascii("噶") && Ascii(t1)<Ascii("哈"))
			        val+="G";
			else if (Ascii(t1)>=Ascii("哈") && Ascii(t1)<Ascii("击"))
			        val+="H";
			else if (Ascii(t1)>=Ascii("击") && Ascii(t1)<Ascii("喀"))
			        val+="J";
			else if (Ascii(t1)>=Ascii("喀") && Ascii(t1)<Ascii("拉"))
			        val+="K";
			else if (Ascii(t1)>=Ascii("拉") && Ascii(t1)<Ascii("妈"))
			        val+="L";
			else if (Ascii(t1)>=Ascii("妈") && Ascii(t1)<Ascii("拿"))
			        val+="M";
			else if (Ascii(t1)>=Ascii("拿") && Ascii(t1)<Ascii("哦"))
			        val+="N";
			else if (Ascii(t1)>=Ascii("哦") && Ascii(t1)<Ascii("啪"))
			        val+="O";
			else if (Ascii(t1)>=Ascii("啪") && Ascii(t1)<Ascii("期"))
			        val+="P";
			else if (Ascii(t1)>=Ascii("期") && Ascii(t1)<Ascii("然"))
			        val+="Q";
			else if (Ascii(t1)>=Ascii("然") && Ascii(t1)<Ascii("撒"))
			        val+="R";
			else if (Ascii(t1)>=Ascii("撒") && Ascii(t1)<Ascii("塌"))
			        val+="S";
			else if (Ascii(t1)>=Ascii("塌") && Ascii(t1)<Ascii("挖"))
			        val+="T";
			else if (Ascii(t1)>=Ascii("挖") && Ascii(t1)<Ascii("昔"))
			        val+="W";
			else if (Ascii(t1)>=Ascii("昔") && Ascii(t1)<Ascii("压"))
			        val+="X";
			else if (Ascii(t1)>=Ascii("压") && Ascii(t1)<Ascii("匝"))
			        val+="Y";
			else if (Ascii(t1)>=Ascii("匝"))
			        val+="Z";
			else 
			        val+="-";
		}
		
		return val.toUpperCase();
	}

	public static int Ascii(String chr)
	{
		byte[] codeBytes= EncodingUtils.getAsciiBytes(chr);
		if (codeBytes.length==2)
		{
			return (int)codeBytes[0]*256+(int)codeBytes[1]-65536;
		}
		else
		{
			return (int)codeBytes[0];
		}
	} 
}
