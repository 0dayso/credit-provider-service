package cn.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.MobileNumberSection;
import cn.repository.MobileNumberSectionReository;
import cn.service.MobileNumberSectionService;
import cn.utils.CommonUtils;

@Service
public class MobileNumberSectionServiceImpl implements MobileNumberSectionService {

	@Autowired
	private MobileNumberSectionReository reository;

	@Override
	public MobileNumberSection findByNumberSection(String numberSection) {
		List<MobileNumberSection> list = reository.findByNumberSection(numberSection);
		return CommonUtils.isNotEmpty(list) ? null : list.get(0);
	}

}
