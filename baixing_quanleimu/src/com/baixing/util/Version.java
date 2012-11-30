package com.baixing.util;

/**
 * Created with IntelliJ IDEA.
 * User: zengming
 * Date: 12-11-8
 * Time: PM3:13
 * 字符串版本号比较
 */
public class Version {
    /**
     * 字符串版本号比较，< 返回 -1，== 返回 0，> 返回 1；非法版本号返回 -2
     * @param v1
     * @param v2
     * @return
     */
    public static int compare(String v1, String v2) {
        if (v1.equals(v2))
            return 0;
        String verReg = "[0-9]+(\\.[0-9]+)*";
        if (!v1.matches(verReg) || !v2.matches(verReg)) {
            return -2;
        }
        String[] vArr1 = v1.split("\\.");
        String[] vArr2 = v2.split("\\.");
        for (int i=0; i < Math.max(vArr1.length, vArr2.length); i++) {
            int k1 = vArr1.length > i ? Integer.valueOf(vArr1[i])  : 0;
            int k2 = vArr2.length > i ? Integer.valueOf(vArr2[i])  : 0;
            if (k1 == k2) {
                continue;
            }
            return k1 < k2 ? -1 : 1;
        }
        return 0;
    }
}
