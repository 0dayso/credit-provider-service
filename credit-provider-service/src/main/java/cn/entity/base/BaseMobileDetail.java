package cn.entity.base;

import java.util.Date;

import org.springframework.data.mongodb.core.index.Indexed;

import cn.utils.Constant;

/**
 * 手机号码实体类 1349卫星通信
 * 
 * @author ChuangLan
 *
 */
public class BaseMobileDetail {

	@Indexed(name = "{'mobile_': 1}")
	private String mobile; // 手机号码

	private String account; // 来源公司账户

	private String delivrd; // 状态

	private String province; // 省份

	private String city; // 城市

	private String signature; // 企业签名

	@Indexed(name = "{'reportTime_': 1}")
	private Date reportTime; // 返回时间

	private Integer platform; // 平台k

	private String content; // 内容

	private String productId; // 产品ID

	private String type; // 短信类型
	
	private Date createTime; // 默认系统当前时间

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;

		// 根据账号简称设置对应类别
		String acc = account.substring(0, 1);
		if (acc.equalsIgnoreCase("N")) {
			this.type = Constant.MOBILE_TYPE_TRADES;
		} else if (acc.equalsIgnoreCase("M")) {
			this.type = Constant.MOBILE_TYPE_SMM;
		} else if (acc.equalsIgnoreCase("I")) {
			this.type = Constant.MOBILE_TYPE_TRANSNATIONAL;
		} else if (acc.equalsIgnoreCase("C")) {
			this.type = Constant.MOBILE_TYPE_MMS;
		} else if (acc.equalsIgnoreCase("Y")) {
			this.type = Constant.MOBILE_TYPE_VOICE;
		} else if (acc.equalsIgnoreCase("D")) {
			acc = account.substring(0, 2);
			if (acc.equalsIgnoreCase("DK")) {
				this.type = Constant.MOBILE_TYPE_FINANCE;
			}
		} else {
			this.type = Constant.MOBILE_TYPE_RESTS;
		}
	}

	public String getDelivrd() {
		return delivrd;
	}

	public void setDelivrd(String delivrd) {
		this.delivrd = delivrd;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public Date getReportTime() {
		return reportTime;
	}

	public void setReportTime(Date reportTime) {
		this.reportTime = reportTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Integer getPlatform() {
		return platform;
	}

	public void setPlatform(Integer platform) {
		this.platform = platform;
	}

	
}
