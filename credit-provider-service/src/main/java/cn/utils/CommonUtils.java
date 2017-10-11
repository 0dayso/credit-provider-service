package cn.utils;

import java.util.Collection;

public class CommonUtils {

	/**
	 * 判断集合是否为空
	 * @param collection
	 * @return
	 */
	public static Boolean isNotEmpty(Collection<?> collection) {
		return (null == collection || collection.size() <= 0);
	}
	
	/**
	 * 判断字符是否为空
	 * @param str
	 * @return
	 */
	public static Boolean isNotString(String str){
		return (null == str || "".equals(str) || "null".equals(str));
	}
	
	public static void main(String[] args) {
		System.out.println(CommonUtils.isNotEmpty(null));
		System.out.println(CommonUtils.isNotString("null"));
	}
}
