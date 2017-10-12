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

import cn.entity.ct.CT1700;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.ct.CT1700Service;

@Service
public class CT1700ServiceImpl implements CT1700Service{
	
	@Autowired
	private BaseMobileDetailRepository<CT1700, String> repository;
	
	@Override
	public List<CT1700> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		Sort sort = new Sort(Direction.DESC,"reportTime");
		Pageable pageable = new PageRequest(1 - 1, 1, sort);
		Page<CT1700> page  = repository.findByMobileAndReportTime(mobile,startTime,endTime,pageable);
		return page.getContent();
	}

	@Override
	public List<CT1700> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
