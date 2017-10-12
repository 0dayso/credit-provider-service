package cn.service.cu.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import cn.entity.cu.CU155;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cu.CU155Service;

@Service
public class CU155ServiceImpl implements CU155Service {

	@Autowired
	private BaseMobileDetailRepository<CU155, String> repository;
	
	@Override
	public List<CU155> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		Sort sort = new Sort(Direction.DESC,"reportTime");
		Pageable pageable = new PageRequest(1 - 1, 1, sort);
		Page<CU155> page  = repository.findByMobileAndReportTime(mobile,startTime,endTime,pageable);
		return page.getContent();
	}

	@Override
	public List<CU155> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
