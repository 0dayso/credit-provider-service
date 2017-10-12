package cn.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import cn.entity.CvsFilePath;
import cn.entity.WaterConsumption;
import cn.entity.base.BaseMobileDetail;
//import cn.redis.RedisClient;
import cn.redis.RedisLock;
import cn.service.CvsFilePathService;
import cn.service.ForeignService;
import cn.service.SpaceDetectionService;
import cn.utils.CommonUtils;
import cn.utils.DateUtils;
import cn.utils.FileUtils;
import cn.utils.UUIDTool;
import main.java.cn.common.BackResult;
import main.java.cn.common.ResultCode;
import main.java.cn.domain.CvsFilePathDomain;
import main.java.cn.domain.RunTestDomian;
import main.java.cn.hhtp.util.HttpUtil;
import main.java.cn.sms.util.ChuangLanSmsUtil;
import net.sf.json.JSONObject;

@Service
public class ForeignServiceImpl implements ForeignService {

	private final static Logger logger = LoggerFactory.getLogger(ForeignServiceImpl.class);

	@Autowired
	private SpaceDetectionService spaceDetectionService;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Value("${server.port}")
	private String port;

	@Value("${loadfilePath}")
	private String loadfilePath;

	// 注入RedisTemplate对象
	@Resource(name = "redisTemplate")
	private RedisTemplate<String, String> redisTemplate;

	private HashMap<String, Object> map = new HashMap<String, Object>();

	@Autowired
	private CvsFilePathService cvsFilePathService;

	@Value("${consumeAccountUrl}")
	private String consumeAccountUrl;

	@Value("${findAccountUrl}")
	private String findAccountUrl;

	public BackResult<RunTestDomian> runTheTest11111(String fileUrl, String userId, String timestamp, String mobile) {

		RunTestDomian runTestDomian = new RunTestDomian();
		BackResult<RunTestDomian> result = new BackResult<RunTestDomian>();

		// String key = "runTheTest_" + userId;

		RedisLock lock = new RedisLock(redisTemplate, "testFile_" + timestamp + "_" + userId, 0, 30 * 60 * 1000);

		BufferedReader br = null;

		try {

			// 处理加锁业务
			if (lock.lock()) {

				logger.info("key[" + "testFile_" + timestamp + "_" + userId + "]");

				map.remove("testCount_" + userId); // 清空条数

				int testCount = 0;
				map.put("testCount_" + userId, testCount);
				logger.info("用户编号：[" + userId + "]文件地址：[" + fileUrl + "]开始执行空号检索事件 事件开始时间："
						+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "post:" + port);

				List<List<Object>> thereDataList = new ArrayList<List<Object>>();
				List<Object> thereRowList = null;
				List<List<Object>> sixDataList = new ArrayList<List<Object>>();
				List<Object> sixRowList = null;
				List<List<Object>> unKonwDataList = new ArrayList<List<Object>>();
				List<Object> unKonwRowList = null;

				// 3个月前的时间
				Date thereStartTime = DateUtils.addDay(DateUtils.getCurrentDateTime(), -90);
				// 6个月前的时间
				Date sixStartTime = DateUtils.addDay(DateUtils.getCurrentDateTime(), -180);

				File file = new File(fileUrl);
				if (file.isFile() && file.exists()) {

					InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
					br = new BufferedReader(isr);
					String lineTxt = null;

					while ((lineTxt = br.readLine()) != null) {

						if (CommonUtils.isNotString(lineTxt)) {
							continue;
						}

						logger.info("执行查询3月数据每条开始时间" + System.currentTimeMillis());
						// 检测 3个月内
						BaseMobileDetail detail = spaceDetectionService.findByMobileAndReportTime(lineTxt,
								thereStartTime, DateUtils.getCurrentDateTime());

						logger.info("执行查询3月数据每条结束" + System.currentTimeMillis());
						// 数据存在 并且状态 为成功
						if (null != detail) {
							if (detail.getDelivrd().equals("DELIVRD")) {
								thereRowList = new ArrayList<Object>();
								thereRowList.add(detail.getMobile());
								thereRowList.add("实号");
								thereDataList.add(thereRowList);
							} else {
								unKonwRowList = new ArrayList<Object>();
								unKonwRowList.add(detail.getMobile());
								unKonwRowList.add(detail.getDelivrd());
								unKonwDataList.add(unKonwRowList);
							}
						} else {

							logger.info("执行查询6月数据每条开始时间" + System.currentTimeMillis());
							// 检测6个月内
							detail = spaceDetectionService.findByMobileAndReportTime(lineTxt, sixStartTime,
									thereStartTime);

							logger.info("执行查询6月数据每条开始时间" + System.currentTimeMillis());

							// 6个月内数据存在 并且状态 为成功
							if (null != detail) {
								if (detail.getDelivrd().equals("DELIVRD")) {
									sixRowList = new ArrayList<Object>();
									sixRowList.add(detail.getMobile());
									sixRowList.add("实号");
									sixDataList.add(sixRowList);
								} else {
									unKonwRowList = new ArrayList<Object>();
									unKonwRowList.add(detail.getMobile());
									unKonwRowList.add(detail.getDelivrd());
									unKonwDataList.add(unKonwRowList);
								}
							} else {
								unKonwRowList = new ArrayList<Object>();
								unKonwRowList.add(lineTxt);
								unKonwRowList.add("未知");
								unKonwDataList.add(unKonwRowList);
							}

						}

						testCount = testCount + 1;
						map.put("testCount_" + userId, testCount);
					}

				} else {
					logger.error("客户ID：[" + userId + "]执行号码检测发现文件地址不存在");
					result.setResultCode(ResultCode.RESULT_BUSINESS_EXCEPTIONS);
					result.setResultMsg("客户ID：[" + userId + "]执行号码检测发现文件地址不存在");
					// 清空
					map.remove("testCount_" + userId);
					lock.unlock(); // 注销锁
					return result;
				}

				// 文件地址入库
				CvsFilePath cvsFilePath = new CvsFilePath();
				cvsFilePath.setUserId(userId);

				// 生成报表
				String filePath = loadfilePath + userId + "/" + DateUtils.getDate() + "/";
				Object[] head = { "手机号码", "状态", };
				if (!CommonUtils.isNotEmpty(thereDataList)) {
					logger.info("实号总条数：" + thereDataList.size());
					FileUtils.createCvsFile("实号.csv", filePath, thereDataList, head);
					cvsFilePath.setThereCount(String.valueOf(thereDataList.size()));
				}

				if (!CommonUtils.isNotEmpty(sixDataList)) {
					logger.info("空号总条数：" + sixDataList.size());
					FileUtils.createCvsFile("空号.csv", filePath, sixDataList, head);
					cvsFilePath.setSixCount(String.valueOf(sixDataList.size()));
				}

				if (!CommonUtils.isNotEmpty(unKonwDataList)) {
					logger.info("未知总条数：" + unKonwDataList.size());
					FileUtils.createCvsFile("未知.csv", filePath, unKonwDataList, head);
					cvsFilePath.setUnknownSize(String.valueOf(unKonwDataList.size()));
				}

				List<File> list = new ArrayList<File>();

				if (!CommonUtils.isNotEmpty(thereDataList)) {
					list.add(new File(filePath + "实号.csv"));
					cvsFilePath.setThereFilePath(userId + "/" + DateUtils.getDate() + "/实号.csv");
					cvsFilePath.setThereFileSize(FileUtils.getFileSize(filePath + "实号.csv"));
				}

				if (!CommonUtils.isNotEmpty(sixDataList)) {
					list.add(new File(filePath + "空号.csv"));
					cvsFilePath.setSixFilePath(userId + "/" + DateUtils.getDate() + "/空号.csv");
					cvsFilePath.setSixFileSize(FileUtils.getFileSize(filePath + "空号.csv"));
				}

				if (!CommonUtils.isNotEmpty(unKonwDataList)) {
					list.add(new File(filePath + "未知.csv"));
					cvsFilePath.setUnknownFilePath(userId + "/" + DateUtils.getDate() + "/未知.csv");
					cvsFilePath.setUnknownFileSize(FileUtils.getFileSize(filePath + "未知.csv"));
				}

				String zipName = "测试结果包.zip";
				// 报表文件打包
				if (null != list && list.size() > 0) {
					zipName = "测试结果包.zip";
					FileUtils.createZip(list, filePath + zipName);
					cvsFilePath.setZipName(zipName);
					cvsFilePath.setZipPath((userId + "/" + DateUtils.getDate() + "/测试结果包.zip"));
					cvsFilePath.setZipSize(FileUtils.getFileSize(filePath + zipName));
				}

				cvsFilePath.setCreateTime(new Date());
				mongoTemplate.save(cvsFilePath);

				// 记账
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("creUserId", userId);
				jsonObject.put("count", testCount);
				logger.info("用户ID发送请求支付消费条数,请求参数:" + jsonObject);
				String responseStr = HttpUtil.createHttpPost(consumeAccountUrl, jsonObject);
				logger.info("用户ID发送请求支付消费条数,请求结果:" + responseStr);
				JSONObject json = JSONObject.fromObject(responseStr);

				if (json.get("resultCode").equals("000000") && json.get("resultObj").equals(Boolean.TRUE)) {
					logger.info("用户ID[" + userId + "]本次成功消费条数：" + testCount);
					// 记录流水记录
					WaterConsumption waterConsumption = new WaterConsumption();
					waterConsumption.setUserId(userId);
					waterConsumption.setId(UUIDTool.getInstance().getUUID());
					waterConsumption.setConsumptionNum("SHJC_" + timestamp);
					waterConsumption.setMenu("客户上传文件实号检测");
					waterConsumption.setStatus("1");
					waterConsumption.setType("1"); // 实号检测
					waterConsumption.setCreateTime(new Date());
					waterConsumption.setCount(String.valueOf(testCount)); // 条数
					waterConsumption.setUpdateTime(new Date());
					mongoTemplate.save(waterConsumption);
				} else {
					result.setResultCode(ResultCode.RESULT_BUSINESS_EXCEPTIONS);
					result.setResultMsg("用户记账失败！");
					return result;
				}

				result.setResultMsg("成功");

				logger.info("用户编号：[" + userId + "]文件地址：[" + fileUrl + "]结束空号检索事件 事件结束时间："
						+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

			} else {

				LineNumberReader rf = null;
				int lines = 0;
				File test = new File(fileUrl);
				File file = new File(fileUrl);
				if (file.isFile() && file.exists()) {
					long fileLength = test.length();
					rf = new LineNumberReader(new FileReader(test));

					if (rf != null) {
						rf.skip(fileLength);
						lines = rf.getLineNumber();
						rf.close();
					}
				}

				if (map.size() > 0) {
					runTestDomian.setRunCount(Integer.valueOf(map.get("testCount_" + userId).toString()));
				} else {
					runTestDomian.setRunCount(0);
				}

				if (lines == Integer.valueOf(map.get("testCount_" + userId).toString())) {
					result.setResultMsg("任务执行结束");
					runTestDomian.setStatus("2"); // 1执行中 2执行结束 3执行异常
				} else {
					result.setResultMsg("任务执行中");
					runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
				}

				result.setResultObj(runTestDomian);
				return result;
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("客户ID：[" + userId + "]执行号码检测出现系统异常：" + e.getMessage());
			lock.unlock(); // 注销锁
			// 清空
			map.remove("testCount_" + userId);
			result.setResultCode(ResultCode.RESULT_FAILED);
			result.setResultMsg("客户ID：[" + userId + "]执行号码检测出现系统异常：" + e.getMessage());
			return result;
		} finally {
			try {

				if (null != br) {
					br.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
				result.setResultCode(ResultCode.RESULT_FAILED);
				result.setResultMsg("客户ID：[" + userId + "]执行号码检测出现系统异常：" + e.getMessage());
				return result;
			}
		}

		runTestDomian.setRunCount(Integer.valueOf(map.get("testCount_" + userId).toString()));
		runTestDomian.setStatus("2"); // 1执行中 2执行结束 3执行异常
		lock.unlock(); // 注销锁
		// 清空
		result.setResultObj(runTestDomian);
		return result;
	}

	@Override
	public BackResult<RunTestDomian> runTheTest(String fileUrl, String userId, String timestamp, String mobile) {

		RunTestDomian runTestDomian = new RunTestDomian();
		BackResult<RunTestDomian> result = new BackResult<RunTestDomian>();

		RedisLock lock = new RedisLock(redisTemplate, "testFile_" + timestamp + "_" + userId, 0, 2 * 60 * 60 * 1000);

		BufferedReader br = null;

		try {

			// 处理加锁业务
			if (lock.lock()) {

				LineNumberReader rf = null;
				int lines = 0;
				File test = new File(fileUrl);
				File file1 = new File(fileUrl);
				if (file1.isFile() && file1.exists()) {
					long fileLength = test.length();
					rf = new LineNumberReader(new FileReader(test));

					if (rf != null) {
						rf.skip(fileLength);
						lines = rf.getLineNumber();
						rf.close();
					}
				}

				// 验证账户余额
				JSONObject jsonAccount = new JSONObject();
				jsonAccount.put("mobile", mobile);
				logger.info("用户发送请求查询账户余额条数,请求参数:" + jsonAccount);
				String responseStr1 = HttpUtil.createHttpPost(findAccountUrl, jsonAccount);
				logger.info("用户发送请求查询账户余额条数,请求结果:" + responseStr1);
				JSONObject responseJson = JSONObject.fromObject(responseStr1);

				if (!responseJson.get("resultCode").equals("000000")) {
					runTestDomian.setStatus("3"); // 1执行中 2执行结束 3执行异常4账户余额不足
					lock.unlock(); // 注销锁
					// 清空
					result.setResultObj(runTestDomian);
					result.setResultMsg("查询账户余额失败");
					return result;
				}

				JSONObject accountJson = JSONObject.fromObject(responseJson.get("resultObj"));

				if (Integer.valueOf(accountJson.get("account").toString()) < lines) {
					runTestDomian.setStatus("4"); // 1执行中 2执行结束 3执行异常4账户余额不足
					lock.unlock(); // 注销锁
					// 清空
					result.setResultObj(runTestDomian);
					result.setResultMsg("账户余额不足");
					return result;
				}

				logger.info("key[" + "testFile_" + timestamp + "_" + userId + "]");

				map.remove("testCount_" + userId); // 清空需要记账的总条数

				map.remove("count_" + userId); // 清空实际检测的总条数

				int testCount = 0; // 需要记账的总条数
				int count = 0; // 实际检测的总条数
				map.put("testCount_" + userId, testCount);
				logger.info("用户编号：[" + userId + "]文件地址：[" + fileUrl + "]开始执行空号检索事件 事件开始时间："
						+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "post:" + port);

				List<List<Object>> thereDataList = new ArrayList<List<Object>>();
				List<Object> thereRowList = null;
				List<List<Object>> sixDataList = new ArrayList<List<Object>>();
				List<Object> sixRowList = null;
				List<List<Object>> unKonwDataList = new ArrayList<List<Object>>();
				List<Object> unKonwRowList = null;

				// 3个月前的时间
				// Date thereStartTime =
				// DateUtils.addDay(DateUtils.getCurrentDateTime(), -90);
				// 6个月前的时间
				Date sixStartTime = DateUtils.addDay(DateUtils.getCurrentDateTime(), -270);

				File file = new File(fileUrl);
				if (file.isFile() && file.exists()) {

					InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
					br = new BufferedReader(isr);
					String lineTxt = null;

					while ((lineTxt = br.readLine()) != null) {

						count = count + 1;
						map.put("count_" + userId, count);
						if (CommonUtils.isNotString(lineTxt)) {
							continue;
						}
						
						//验证是否为正常的１１位有效数字
						if (!CommonUtils.isNumeric(lineTxt)) {
							continue;
						}

						// 检测 3个月内
						BaseMobileDetail detail = spaceDetectionService.findByMobileAndReportTime(lineTxt, sixStartTime,
								DateUtils.getCurrentDateTime());

						if (null != detail) {
							if (detail.getDelivrd().equals("DELIVRD") || detail.getDelivrd().equals("MC:0055")
									|| detail.getDelivrd().equals("CJ:0007") || detail.getDelivrd().equals("CJ:0008")
									|| detail.getDelivrd().equals("DB:0141") || detail.getDelivrd().equals("DISTURB")
									|| detail.getDelivrd().equals("HD:0001") || detail.getDelivrd().equals("IC:0151")
									|| detail.getDelivrd().equals("ID:0004") || detail.getDelivrd().equals("MBBLACK")
									|| detail.getDelivrd().equals("MC:0055") || detail.getDelivrd().equals("MK:0008")
									|| detail.getDelivrd().equals("MK:0010") || detail.getDelivrd().equals("MK:0022")
									|| detail.getDelivrd().equals("MK:0024") || detail.getDelivrd().equals("MK:0029")
									|| detail.getDelivrd().equals("MN:0017") || detail.getDelivrd().equals("MN:0044")
									|| detail.getDelivrd().equals("MN:0051") || detail.getDelivrd().equals("MN:0054")
									|| detail.getDelivrd().equals("REJECT") || detail.getDelivrd().equals("SME19")
									|| detail.getDelivrd().equals("TIMEOUT") || detail.getDelivrd().equals("UNDELIV")
									|| detail.getDelivrd().equals("GG:0024") || detail.getDelivrd().equals("DB:0309")
									|| detail.getDelivrd().equals("SME92") || detail.getDelivrd().equals("DB:0114")
									|| detail.getDelivrd().equals("MN:0174") || detail.getDelivrd().equals("YX:7000")
									|| detail.getDelivrd().equals("MK:0004") || detail.getDelivrd().equals("NOROUTE")
									|| detail.getDelivrd().equals("CJ:0005") || detail.getDelivrd().equals("IC:0055")
									|| detail.getDelivrd().equals("REJECTE") || detail.getDelivrd().equals("MN:0053")
									|| detail.getDelivrd().equals("MB:1026")) {
								thereRowList = new ArrayList<Object>();
								thereRowList.add(detail.getMobile());
//								thereRowList.add("");
//								thereRowList.add("");
								thereDataList.add(thereRowList);
							} else if (detail.getDelivrd().equals("101") || detail.getDelivrd().equals("-1")
									|| detail.getDelivrd().equals("SGIP:2：12") || detail.getDelivrd().equals("ERRNUM")
									|| detail.getDelivrd().equals("RP:1") || detail.getDelivrd().equals("MN:0001")
									|| detail.getDelivrd().equals("SPMSERR:136")
									|| detail.getDelivrd().equals("MK:0000") || detail.getDelivrd().equals("MK:0001")
									|| detail.getDelivrd().equals("SGIP:1") || detail.getDelivrd().equals("SGIP:33")
									|| detail.getDelivrd().equals("SGIP:67") || detail.getDelivrd().equals("LT:0001")
									|| detail.getDelivrd().equals("3") || detail.getDelivrd().equals("Deliver")
									|| detail.getDelivrd().equals("CB:0001") || detail.getDelivrd().equals("CB:0053")
									|| detail.getDelivrd().equals("DB:0101") || detail.getDelivrd().equals("12")
									|| detail.getDelivrd().equals("12") || detail.getDelivrd().equals("601")
									|| detail.getDelivrd().equals("MK:0012")) {
								sixRowList = new ArrayList<Object>();
								sixRowList.add(detail.getMobile());
								sixRowList.add("空号");
//								sixRowList.add(detail.getDelivrd());
								sixDataList.add(sixRowList);
							} else if (detail.getDelivrd().equals("HD:31") || detail.getDelivrd().equals("IC:0001")
									|| detail.getDelivrd().equals("MI:0011") || detail.getDelivrd().equals("MI:0013")
									|| detail.getDelivrd().equals("MI:0029") || detail.getDelivrd().equals("MK:0005")
									
									|| detail.getDelivrd().equals("UNKNOWN") || detail.getDelivrd().equals("MI:0024")
									|| detail.getDelivrd().equals("MI:0054") || detail.getDelivrd().equals("MN:0059")
									|| detail.getDelivrd().equals("MI:0059") || detail.getDelivrd().equals("MI:0055")
									|| detail.getDelivrd().equals("MI:0004") || detail.getDelivrd().equals("MI:0005")) {
								sixRowList = new ArrayList<Object>();
								sixRowList.add(detail.getMobile());
								sixRowList.add("停机");
//								sixRowList.add(detail.getDelivrd());
								sixDataList.add(sixRowList);
							} else {
//								unKonwRowList = new ArrayList<Object>();
//								unKonwRowList.add(lineTxt);
////								unKonwRowList.add("未知");
////								unKonwRowList.add(detail.getDelivrd());
//								unKonwDataList.add(unKonwRowList);
								
								thereRowList = new ArrayList<Object>();
								thereRowList.add(lineTxt);
//								thereRowList.add("");
//								thereRowList.add("");
								thereDataList.add(thereRowList);
								
							}
						} else {
							
							thereRowList = new ArrayList<Object>();
							thereRowList.add(lineTxt);
//							thereRowList.add("");
//							thereRowList.add("");
							thereDataList.add(thereRowList);
							
							
							
//							unKonwRowList = new ArrayList<Object>();
//							unKonwRowList.add(lineTxt);
////							unKonwRowList.add("未知");
////							unKonwRowList.add("");
//							unKonwDataList.add(unKonwRowList);
						}

						testCount = testCount + 1;
						map.put("testCount_" + userId, testCount);
					}

				} else {
					logger.error("客户ID：[" + userId + "]执行号码检测发现文件地址不存在");
					result.setResultCode(ResultCode.RESULT_BUSINESS_EXCEPTIONS);
					result.setResultMsg("客户ID：[" + userId + "]执行号码检测发现文件地址不存在");
					// 清空
					map.remove("testCount_" + userId);
					map.remove("count_" + userId); // 清空实际检测的总条数
					lock.unlock(); // 注销锁
					return result;
				}

				// 文件地址入库
				CvsFilePath cvsFilePath = new CvsFilePath();
				cvsFilePath.setUserId(userId);

				// 生成报表
				String filePath = loadfilePath + userId + "/" + DateUtils.getDate() + "/";
//				Object[] head = { "手机号码", "状态", "代码状态" };
				if (!CommonUtils.isNotEmpty(thereDataList)) {
					logger.info("实号总条数：" + thereDataList.size());
					Object[] shhead = { "手机号码" };
					FileUtils.createCvsFile("实号.csv", filePath, thereDataList, shhead);
					cvsFilePath.setThereCount(String.valueOf(thereDataList.size()));
				}

				if (!CommonUtils.isNotEmpty(sixDataList)) {
					logger.info("空号总条数：" + sixDataList.size());
					Object[] head = { "手机号码", "状态" };
					FileUtils.createCvsFile("空号.csv", filePath, sixDataList, head);
					cvsFilePath.setSixCount(String.valueOf(sixDataList.size()));
				}

//				if (!CommonUtils.isNotEmpty(unKonwDataList)) {
//					logger.info("未知总条数：" + unKonwDataList.size());
//					Object[] wzhead = { "手机号码" };
//					FileUtils.createCvsFile("未知.csv", filePath, unKonwDataList, wzhead);
//					cvsFilePath.setUnknownSize(String.valueOf(unKonwDataList.size()));
//				}

				List<File> list = new ArrayList<File>();

				if (!CommonUtils.isNotEmpty(thereDataList)) {
					list.add(new File(filePath + "实号.csv"));
					cvsFilePath.setThereFilePath(userId + "/" + DateUtils.getDate() + "/实号.csv");
					cvsFilePath.setThereFileSize(FileUtils.getFileSize(filePath + "实号.csv"));
				}

				if (!CommonUtils.isNotEmpty(sixDataList)) {
					list.add(new File(filePath + "空号.csv"));
					cvsFilePath.setSixFilePath(userId + "/" + DateUtils.getDate() + "/空号.csv");
					cvsFilePath.setSixFileSize(FileUtils.getFileSize(filePath + "空号.csv"));
				}

//				if (!CommonUtils.isNotEmpty(unKonwDataList)) {
//					list.add(new File(filePath + "未知.csv"));
//					cvsFilePath.setUnknownFilePath(userId + "/" + DateUtils.getDate() + "/未知.csv");
//					cvsFilePath.setUnknownFileSize(FileUtils.getFileSize(filePath + "未知.csv"));
//				}

				String zipName = "测试结果包.zip";
				// 报表文件打包
				if (null != list && list.size() > 0) {
					zipName = "测试结果包.zip";
					FileUtils.createZip(list, filePath + zipName);
					cvsFilePath.setZipName(zipName);
					cvsFilePath.setZipPath((userId + "/" + DateUtils.getDate() + "/测试结果包.zip"));
					cvsFilePath.setZipSize(FileUtils.getFileSize(filePath + zipName));
				}

				cvsFilePath.setCreateTime(new Date());
				mongoTemplate.save(cvsFilePath);

				// 记账
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("creUserId", userId);
				jsonObject.put("count", testCount);
				logger.info("用户ID发送请求支付消费条数,请求参数:" + jsonObject);
				String responseStr = HttpUtil.createHttpPost(consumeAccountUrl, jsonObject);
				logger.info("用户ID发送请求支付消费条数,请求结果:" + responseStr);
				JSONObject json = JSONObject.fromObject(responseStr);

				if (json.get("resultCode").equals("000000") && json.get("resultObj").equals(Boolean.TRUE)) {
					logger.info("用户ID[" + userId + "]本次成功消费条数：" + testCount);
					// 记录流水记录
					WaterConsumption waterConsumption = new WaterConsumption();
					waterConsumption.setUserId(userId);
					waterConsumption.setId(UUIDTool.getInstance().getUUID());
					waterConsumption.setConsumptionNum("SHJC_" + timestamp);
					waterConsumption.setMenu("客户上传文件实号检测");
					waterConsumption.setStatus("1");
					waterConsumption.setType("1"); // 实号检测
					waterConsumption.setCreateTime(new Date());
					waterConsumption.setCount(String.valueOf(testCount)); // 条数
					waterConsumption.setUpdateTime(new Date());
					mongoTemplate.save(waterConsumption);
				} else {
					result.setResultCode(ResultCode.RESULT_BUSINESS_EXCEPTIONS);
					result.setResultMsg("用户记账失败！");
					return result;
				}

				// 发送短信
				ChuangLanSmsUtil.getInstance().sendSmsByMobileForTest(mobile);
				
				result.setResultMsg("成功");

				logger.info("用户编号：[" + userId + "]文件地址：[" + fileUrl + "]结束空号检索事件 事件结束时间："
						+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

			} else {

				LineNumberReader rf = null;
				int lines = 0;
				File test = new File(fileUrl);
				File file = new File(fileUrl);
				if (file.isFile() && file.exists()) {
					long fileLength = test.length();
					rf = new LineNumberReader(new FileReader(test));

					if (rf != null) {
						rf.skip(fileLength);
						lines = rf.getLineNumber();
						rf.close();
					}
				}

				if (map.size() > 0) {
					runTestDomian.setRunCount(Integer.valueOf(map.get("count_" + userId).toString()));
				} else {
					runTestDomian.setRunCount(0);
				}

				logger.info("lines: " + lines + "count_:" + map.get("count_" + userId).toString());

				if (lines <= Integer.valueOf(map.get("count_" + userId).toString())) {
					result.setResultMsg("任务执行结束");
					runTestDomian.setStatus("2"); // 1执行中 2执行结束 3执行异常
					lock.unlock(); // 注销锁

					logger.info("手机号码：" + mobile + "结束运行检测任务");
					
				} else {
					result.setResultMsg("任务执行中");
					runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
				}

				result.setResultObj(runTestDomian);
				return result;
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("客户ID：[" + userId + "]执行号码检测出现系统异常：" + e.getMessage());
			lock.unlock(); // 注销锁
			// 清空
			
			// 异常发送短信
			ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestEx(mobile);
			
			map.remove("testCount_" + userId);
			map.remove("count_" + userId); // 清空实际检测的总条数
			result.setResultCode(ResultCode.RESULT_FAILED);
			result.setResultMsg("客户ID：[" + userId + "]执行号码检测出现系统异常：" + e.getMessage());
			return result;
		} finally {
			try {

				if (null != br) {
					br.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
				
				// 异常发送短信
				ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestEx(mobile);
				result.setResultCode(ResultCode.RESULT_FAILED);
				result.setResultMsg("客户ID：[" + userId + "]执行号码检测出现系统异常：" + e.getMessage());
				return result;
			}
		}

		runTestDomian.setRunCount(Integer.valueOf(map.get("testCount_" + userId).toString()));
		runTestDomian.setStatus("2"); // 1执行中 2执行结束 3执行异常
		lock.unlock(); // 注销锁
		// 清空
		result.setResultObj(runTestDomian);
		return result;
	}

	@Override
	public BackResult<List<CvsFilePathDomain>> findByUserId(String userId) {

		BackResult<List<CvsFilePathDomain>> result = new BackResult<List<CvsFilePathDomain>>();

		List<CvsFilePathDomain> list = new ArrayList<CvsFilePathDomain>();

		try {
			List<CvsFilePath> listCvsFilePath = cvsFilePathService.findByUserId(userId);

			if (CommonUtils.isNotEmpty(listCvsFilePath)) {
				result.setResultMsg("改用户没有订单信息");
			}

			for (CvsFilePath cvsFilePath : listCvsFilePath) {
				CvsFilePathDomain domain = new CvsFilePathDomain();
				BeanUtils.copyProperties(cvsFilePath, domain);
				list.add(domain);
			}

			result.setResultObj(list);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("客户ID：[" + userId + "]查询下载列表系统异常：" + e.getMessage());
			result.setResultCode(ResultCode.RESULT_FAILED);
			result.setResultMsg("客户ID：[" + userId + "]查询下载列表系统异常：" + e.getMessage());
		}

		return result;
	}

	@Override
	public BackResult<Boolean> deleteCvsByIds(String ids, String userId) {

		logger.info("用户ID：【" + userId + "执行删除下载记录");

		BackResult<Boolean> result = new BackResult<Boolean>();

		try {
			cvsFilePathService.deleteByIds(ids);
			result.setResultObj(Boolean.TRUE);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("客户ID：[" + userId + "]执行删除下载记录系统异常：" + e.getMessage());
			result.setResultCode(ResultCode.RESULT_FAILED);
			result.setResultMsg("客户ID：[" + userId + "]查询下载列表系统异常：" + e.getMessage());
			result.setResultObj(Boolean.FALSE);
		}
		return result;
	}

	public static void main(String[] args) {
		// http://127.0.0.1:8767/userAccount/consumeAccount?creUserId=1598&count=100
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("creUserId", 1598);
		jsonObject.put("count", 100);
		String responseStr = HttpUtil.createHttpPost("http://127.0.0.1:8767/userAccount/consumeAccount", jsonObject);
		JSONObject json = JSONObject.fromObject(responseStr);

		if (json.get("resultCode").equals("000000") && json.get("resultObj").equals(Boolean.TRUE)) {
		}
		// System.out.println(responseStr);
	}

}
