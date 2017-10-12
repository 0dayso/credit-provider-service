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

import cn.entity.cu.CU132;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cu.CU132Service;

@Service
public class CU132ServiceImpl implements CU132Service {

	@Autowired
	private BaseMobileDetailRepository<CU132, String> repository;
	
	@Override
	public List<CU132> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		Sort sort = new Sort(Direction.DESC,"reportTime");
		Pageable pageable = new PageRequest(1 - 1, 1, sort);
		Page<CU132> page  = repository.findByMobileAndReportTime(mobile,startTime,endTime,pageable);
		return page.getContent();
	}

	@Override
	public List<CU132> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
