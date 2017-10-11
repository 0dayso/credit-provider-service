package cn.task.handler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import cn.entity.base.BaseMobileDetail;
import cn.utils.CommonUtils;

/**
 * 步云 数据入库
 * 
 * @author ChuangLan
 *
 */
@Component
public class ByDateSaveDBHandler extends DataListSaveDBHandler {

	private final static Logger logger = LoggerFactory
			.getLogger(ByDateSaveDBHandler.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public void execution(List<BaseMobileDetail> list) {

		if (CommonUtils.isNotEmpty(list)) {
			return;
		}

		try {
			mongoTemplate.insertAll(list);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("=====步云数据执行数据入库出现异常：" + e.getMessage());
		}
	}

}
