package cn.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.service.ForeignService;
import main.java.cn.common.BackResult;
import main.java.cn.domain.CvsFilePathDomain;
import main.java.cn.domain.RunTestDomian;

@RestController
@RequestMapping("/credit")
public class CreditController {

	@Autowired
	private ForeignService foreignService;
	
	@RequestMapping(value = "/runTheTest", method = RequestMethod.GET)
	public BackResult<RunTestDomian> runTheTest(HttpServletRequest request, HttpServletResponse response,String fileUrl,String userId, String timestamp,String mobile) {
		
		return foreignService.runTheTest(fileUrl, userId,timestamp,mobile);
	}
	
	@RequestMapping(value = "/findByUserId", method = RequestMethod.GET)
	public BackResult<List<CvsFilePathDomain>> findByUserId(HttpServletRequest request, HttpServletResponse response,String userId) {
		
		return foreignService.findByUserId(userId);
	}
	
	@RequestMapping(value = "/deleteCvsByIds", method = RequestMethod.GET)
	public BackResult<Boolean> deleteCvsByIds(HttpServletRequest request, HttpServletResponse response,String ids,String userId){
		
		return foreignService.deleteCvsByIds(ids, userId);
	}

}
