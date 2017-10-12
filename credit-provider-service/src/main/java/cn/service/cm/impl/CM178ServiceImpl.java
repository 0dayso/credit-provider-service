package cn.service.cm.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import cn.entity.cm.CM178;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cm.CM178Service;

@Service
public class CM178ServiceImpl implements CM178Service {

	@Autowired
	private BaseMobileDetailRepository<CM178, String> repository;
	
	@Override
	public List<CM178> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		Sort sort = new Sort(Direction.DESC,"reportTime");
		Pageable pageable = new PageRequest(1 - 1, 1, sort);
		Page<CM178> page  = repository.findByMobileAndReportTime(mobile,startTime,endTime,pageable);
		return page.getContent();
	}

	@Override
	public List<CM178> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
