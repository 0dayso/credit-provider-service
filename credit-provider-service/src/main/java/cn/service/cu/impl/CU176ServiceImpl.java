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

import cn.entity.cu.CU176;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cu.CU176Service;

@Service
public class CU176ServiceImpl implements CU176Service {

	@Autowired
	private BaseMobileDetailRepository<CU176, String> repository;
	
	@Override
	public List<CU176> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		Sort sort = new Sort(Direction.DESC,"reportTime");
		Pageable pageable = new PageRequest(1 - 1, 1, sort);
		Page<CU176> page  = repository.findByMobileAndReportTime(mobile,startTime,endTime,pageable);
		return page.getContent();
	}

	@Override
	public List<CU176> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
