package cn.service.unknown.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import cn.entity.unknown.UnknownMobileDetail;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.unknown.UnknownMobileDetailService;

@Service
public class UnknownMobileDetailServiceImpl implements UnknownMobileDetailService {
	
	@Autowired
	private BaseMobileDetailRepository<UnknownMobileDetail, String> repository;
	
	@Override
	public List<UnknownMobileDetail> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		Sort sort = new Sort(Direction.DESC,"reportTime");
		Pageable pageable = new PageRequest(1 - 1, 1, sort);
		Page<UnknownMobileDetail> page  = repository.findByMobileAndReportTime(mobile,startTime,endTime,pageable);
		return page.getContent();
	}

	@Override
	public List<UnknownMobileDetail> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
