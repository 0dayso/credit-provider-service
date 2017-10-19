package cn.entity;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 手机号码段
 * @author ChuangLan
 *
 */
@Document(collection="mobileNumberSection")
public class MobileNumberSection implements Serializable{
	
	private static final long serialVersionUID = 7602103130021437596L;
	
	@Id
	private String id;
	
	private String prefix; // 前三位
	
	@Indexed(name = "{'numberSection_': 1}")
	private String numberSection; // 号段

	private String province; // 省份
	
	private String city; // 城市
	
	private String isp; // 运营商
	
	private String postCode; // 端口号
	
	private String cityCode; // 城市编码
	
	private String areaCode; // 区域编码
	
	private String mobilePhoneType; //运营商

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getNumberSection() {
		return numberSection;
	}

	public void setNumberSection(String numberSection) {
		this.numberSection = numberSection;
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

	public String getIsp() {
		return isp;
	}

	public void setIsp(String isp) {
		this.isp = isp;
	}

	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public String getMobilePhoneType() {
		return mobilePhoneType;
	}

	public void setMobilePhoneType(String mobilePhoneType) {
		this.mobilePhoneType = mobilePhoneType;
	}
	
}
