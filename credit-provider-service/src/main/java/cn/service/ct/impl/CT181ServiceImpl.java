package cn.service.ct.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import cn.entity.ct.CT181;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.ct.CT181Service;

@Service
public class CT181ServiceImpl implements CT181Service{
	
	@Autowired
	private BaseMobileDetailRepository<CT181, String> repository;
	
	@Override
	public List<CT181> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		Sort sort = new Sort(Direction.DESC,"reportTime");
		Pageable pageable = new PageRequest(1 - 1, 1, sort);
		Page<CT181> page  = repository.findByMobileAndReportTime(mobile,startTime,endTime,pageable);
		return page.getContent();
	}

	@Override
	public List<CT181> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
