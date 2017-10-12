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

import cn.entity.cu.CU145;
import cn.repository.base.BaseMobileDetailRepository;
import cn.service.cu.CU145Service;

@Service
public class CU145ServiceImpl implements CU145Service {

	@Autowired
	private BaseMobileDetailRepository<CU145, String> repository;
	
	@Override
	public List<CU145> findByMobileAndReportTime(String mobile, Date startTime, Date endTime) {
		Sort sort = new Sort(Direction.DESC,"reportTime");
		Pageable pageable = new PageRequest(1 - 1, 1, sort);
		Page<CU145> page  = repository.findByMobileAndReportTime(mobile,startTime,endTime,pageable);
		return page.getContent();
	}

	@Override
	public List<CU145> findByMobile(String mobile) {
		return repository.findByMobile(mobile);
	}

}
