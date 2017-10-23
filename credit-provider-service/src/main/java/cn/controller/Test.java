package cn.controller;

import main.java.cn.hhtp.util.HttpUtil;
import main.java.cn.hhtp.util.MD5Util;
import net.sf.json.JSONObject;

public class Test {
	public static void main1(String[] args) {
		// 验证账户余额
		JSONObject jsonAccount = new JSONObject();
		
		String timestamp = String.valueOf(System.currentTimeMillis()).substring(0, 10);
		String tokenValue = MD5Util.getInstance().getMD5Code(timestamp + "chuanglan_data_key_test");
		jsonAccount.put("mobile", "13817367247");
		jsonAccount.put("timestamp", timestamp);
		jsonAccount.put("token", tokenValue);
		System.out.println("用户发送请求查询账户余额条数,请求参数:" + jsonAccount);
		String responseStr1 = HttpUtil.createHttpPost("http://172.16.4.218:8765/userAccount/api/findbyUserAccount", jsonAccount);
		System.out.println("用户发送请求查询账户余额条数,请求结果:" + responseStr1);
//		JSONObject responseJson = JSONObject.fromObject(responseStr1);

	}
	
	public static void main2(String[] args) {
		// 验证充值
		JSONObject jsonAccount = new JSONObject();
		
		String timestamp = String.valueOf(System.currentTimeMillis()).substring(0, 10);
		String tokenValue = MD5Util.getInstance().getMD5Code(timestamp + "chuanglan_data_key_test");
		jsonAccount.put("mobile", "13817367247");
		jsonAccount.put("clOrderNo", "7B1234561718119");
		jsonAccount.put("productsId", "4");
		jsonAccount.put("money", "10000.00");
		jsonAccount.put("payTime", "2017-10-20 13:15:12");
		jsonAccount.put("type", "2");
		jsonAccount.put("timestamp", timestamp);
		jsonAccount.put("token", tokenValue);
		System.out.println("用户支付或者退款,请求参数:" + jsonAccount);
		String responseStr1 = HttpUtil.createHttpPost("http://172.16.4.218:8765/userAccount/api/rechargeOrRefunds", jsonAccount);
		System.out.println("用户支付或者退款,请求结果:" + responseStr1);
//		JSONObject responseJson = JSONObject.fromObject(responseStr1);

	}
	
	public static void main(String[] args) {
		// 验证充值
		JSONObject jsonAccount = new JSONObject();
		
		// https://terp.253.com/realPhoneTestApi/order
		
		String timestamp = String.valueOf(System.currentTimeMillis()).substring(0, 10);
		String tokenValue = MD5Util.getInstance().getMD5Code(timestamp + "chuanglan_real_phone_test_8888");
		jsonAccount.put("account_name", "1381736724711");
		jsonAccount.put("money", "20.00");
		jsonAccount.put("amount", "10000");
		jsonAccount.put("bank", "5");
		jsonAccount.put("pay_mode", "1");
		jsonAccount.put("remark", "备注（选填），长度最大为1000个字符");
		jsonAccount.put("sequence", "CLSH_1508469324118111");
		jsonAccount.put("timestamp", timestamp);
		jsonAccount.put("token", tokenValue);
		System.out.println("下单,请求参数:" + jsonAccount);
		String responseStr1 = HttpUtil.createHttpPost("https://terp.253.com/realPhoneTestApi/order", jsonAccount);
		System.out.println("下单,请求结果:" + responseStr1);
//		JSONObject responseJson = JSONObject.fromObject(responseStr1);

	}
}
