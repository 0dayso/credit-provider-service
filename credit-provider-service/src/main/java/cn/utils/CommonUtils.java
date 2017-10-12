package cn.utils;

import java.util.Collection;

public class CommonUtils {

	/**
	 * 判断集合是否为空
	 * 
	 * @param collection
	 * @return
	 */
	public static Boolean isNotEmpty(Collection<?> collection) {
		return (null == collection || collection.size() <= 0);
	}

	/**
	 * 判断字符是否为空
	 * 
	 * @param str
	 * @return
	 */
	public static Boolean isNotString(String str) {
		return (null == str || "".equals(str));
	}

	/**
	 * 验证是否为13位有效数字
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		if (str.length() != 11) {
			return false;
		}
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	public static void main(String[] args) {
		System.out.println(CommonUtils.isNotEmpty(null));
		System.out.println(CommonUtils.isNotString("111"));
		System.out.println(CommonUtils.isNumeric("~！@#￥%……&*（）——"));
	}

	
}
