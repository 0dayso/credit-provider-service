package cn.task.helper;

import java.util.UUID;

import org.springframework.util.StringUtils;

import cn.entity.base.BaseMobileDetail;
import cn.entity.cm.CM134;
import cn.entity.cm.CM135;
import cn.entity.cm.CM136;
import cn.entity.cm.CM137;
import cn.entity.cm.CM138;
import cn.entity.cm.CM139;
import cn.entity.cm.CM147;
import cn.entity.cm.CM150;
import cn.entity.cm.CM151;
import cn.entity.cm.CM152;
import cn.entity.cm.CM157;
import cn.entity.cm.CM158;
import cn.entity.cm.CM159;
import cn.entity.cm.CM1705;
import cn.entity.cm.CM178;
import cn.entity.cm.CM182;
import cn.entity.cm.CM183;
import cn.entity.cm.CM184;
import cn.entity.cm.CM187;
import cn.entity.cm.CM188;
import cn.entity.ct.CT133;
import cn.entity.ct.CT153;
import cn.entity.ct.CT1700;
import cn.entity.ct.CT177;
import cn.entity.ct.CT180;
import cn.entity.ct.CT181;
import cn.entity.ct.CT189;
import cn.entity.cu.CU130;
import cn.entity.cu.CU131;
import cn.entity.cu.CU132;
import cn.entity.cu.CU145;
import cn.entity.cu.CU155;
import cn.entity.cu.CU156;
import cn.entity.cu.CU1709;
import cn.entity.cu.CU176;
import cn.entity.cu.CU185;
import cn.entity.cu.CU186;
import cn.entity.unknown.UnknownMobileDetail;

public class MobileDetailHelper {

	private static MobileDetailHelper mobileDetailHelper;

	public static MobileDetailHelper getInstance() {
		if (mobileDetailHelper == null) {
			synchronized (MobileDetailHelper.class) {
				if (mobileDetailHelper == null) {
					mobileDetailHelper = new MobileDetailHelper();
				}
			}
		}
		return mobileDetailHelper;
	}

	/**
	 * 根据手机号码段 生成对应的对象
	 * @param mobile
	 * @return
	 */
	public BaseMobileDetail getBaseMobileDetail(String mobile) {

		if (StringUtils.isEmpty(mobile)) {
			return null;
		}

//		String UUID.randomUUID().toString().replace("-", "") = UUUUID.randomUUID().toString().replace("-", "")Tool.getInstance().getUUUUID.randomUUID().toString().replace("-", "")();
		
		BaseMobileDetail detail = null;

		String mob = mobile.substring(0, 3);

		switch (mob) {
		case "170":
			//  分3个 1700 1705 1709
			String mobi = mobile.substring(0, 4);
			if (mobi.equals("1700")) {
				detail = new CT1700(UUID.randomUUID().toString().replace("-", ""));
			} else  if (mobi.equals("1705")) {
				detail = new CM1705(UUID.randomUUID().toString().replace("-", ""));
			} else if (mobi.equals("1709")) {
				detail = new CU1709(UUID.randomUUID().toString().replace("-", ""));
			} else {
				detail = new UnknownMobileDetail(UUID.randomUUID().toString().replace("-", ""));
			}
			break;
		case "134":
			detail = new CM134(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "135":
			detail = new CM135(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "136":
			detail = new CM136(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "137":
			detail = new CM137(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "138":
			detail = new CM138(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "139":
			detail = new CM139(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "147":
			detail = new CM147(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "150":
			detail = new CM150(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "151":
			detail = new CM151(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "152":
			detail = new CM152(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "157":
			detail = new CM157(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "158":
			detail = new CM158(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "159":
			detail = new CM159(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "178":
			detail = new CM178(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "182":
			detail = new CM182(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "183":
			detail = new CM183(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "184":
			detail = new CM184(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "187":
			detail = new CM187(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "188":
			detail = new CM188(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "133":
			detail = new CT133(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "153":
			detail = new CT153(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "177":
			detail = new CT177(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "180":
			detail = new CT180(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "181":
			detail = new CT181(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "189":
			detail = new CT189(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "130":
			detail = new CU130(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "131":
			detail = new CU131(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "132":
			detail = new CU132(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "145":
			detail = new CU145(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "155":
			detail = new CU155(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "156":
			detail = new CU156(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "176":
			detail = new CU176(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "185":
			detail = new CU185(UUID.randomUUID().toString().replace("-", ""));
			break;
		case "186":
			detail = new CU186(UUID.randomUUID().toString().replace("-", ""));
			break;
		default:
			detail = new UnknownMobileDetail(UUID.randomUUID().toString().replace("-", ""));
			break;
		}
		
		return detail;
	}

}
