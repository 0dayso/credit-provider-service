package cn.task.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import cn.entity.base.BaseMobileDetail;
import cn.service.SpaceDetectionService;
import cn.utils.Constant;

/**
 * 创蓝 数据入库
 * 
 * @author ChuangLan
 *
 */
@Component
public class ClDateSaveDBHandler extends DataSaveDBHandler {

	private final static Logger logger = LoggerFactory.getLogger(ClDateSaveDBHandler.class);

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private SpaceDetectionService spaceDetectionService;

	@Override
	public void execution(BaseMobileDetail mobileDetail) {

		if (null == mobileDetail) {
			return;
		}

		try {
			
			BaseMobileDetail detail = spaceDetectionService.findByMobile(mobileDetail.getMobile());
			
			if (null == detail || !detail.getDelivrd().equals("DELIVRD")) {
				mobileDetail.setPlatform(Constant.PLATFORM_CL);

				mongoTemplate.save(mobileDetail);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("=====手机号码为：[" + mobileDetail.getMobile() + "]执行数据入库出现异常：" + e.getMessage());
		}

	}

}
