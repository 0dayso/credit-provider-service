package cn.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import cn.entity.base.BaseMobileDetail;
import cn.task.handler.ByDateSaveDBHandler;
import cn.task.handler.ClDateSaveDBHandler;
import cn.task.helper.MobileDetailHelper;
import cn.utils.CommonUtils;
import cn.utils.DateUtils;

/**
 * 每日定时任务入库
 * 
 * @author ChuangLan
 *
 */
@Component
@Configuration
@EnableScheduling
public class TodayDataSaveDBTask {

	@Value("${spring.data.elasticsearch.cluster-name}")
	private String clusterName;

	@Value("${spring.data.elasticsearch.cluster-nodes}")
	private String clusterNodes;
	@Value("${spring.data.elasticsearch.cluster-port}")
	private int clusterPort;

	@Autowired
	private ClDateSaveDBHandler clDateSaveDBHandler;

	@Autowired
	private ByDateSaveDBHandler byDateSaveDBHandler;

	private final static Logger logger = LoggerFactory
			.getLogger(TodayDataSaveDBTask.class);

	// 该任务执行一次 时间 秒 分 时 天 月 年
	@Scheduled(cron = "0 39 15 28 09 ?")
	public void ClDateSaveDbTask() {
		logger.info("=====开始执行创蓝数据入库操作，任务开始时间:" + DateUtils.getNowTime()
				+ "=====");

		try {
			Settings settings = Settings.builder()
					.put("cluster.name", clusterName)
					.put("client.transport.sniff", true)
					.put("client.transport.ping_timeout", "25s").build();

			@SuppressWarnings("resource")
			TransportClient client = new PreBuiltTransportClient(settings)
					.addTransportAddress(new InetSocketTransportAddress(
							InetAddress.getByName(clusterNodes), clusterPort));

			SearchResponse scrollResp = client
					.prepareSearch("201701", "201701")
					.addSort("_doc", SortOrder.ASC)
					.setScroll(new TimeValue(60000)).setSize(100).get();

			do {
				for (SearchHit hit : scrollResp.getHits().getHits()) {

					String json = hit.getSourceAsString();

					JSONObject backjson = (JSONObject) JSONObject.parse(json);

					String mobile = backjson.getString("mobile");

					BaseMobileDetail detail = MobileDetailHelper.getInstance()
							.getBaseMobileDetail(mobile);
					detail.setAccount(backjson.getString("account"));
					detail.setCity(backjson.getString("city"));
					detail.setContent(backjson.getString("content"));
					detail.setDelivrd(backjson.getString("delivrd"));
					detail.setMobile(backjson.getString("mobile"));
					detail.setProductId(backjson.getString("productId"));
					detail.setProvince(backjson.getString("province"));
					detail.setReportTime(DateUtils.parseDate(
							backjson.getString("reportTime"),
							"yyyy-MM-dd hh:mm:ss"));
					detail.setSignature(backjson.getString("signature"));
					detail.setCreateTime(DateUtils.getCurrentDateTime());
					clDateSaveDBHandler.execution(detail);
				}

				scrollResp = client
						.prepareSearchScroll(scrollResp.getScrollId())
						.setScroll(new TimeValue(60000)).execute().actionGet();
			} while (scrollResp.getHits().getHits().length != 0);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("=====执行创蓝数据入库出现异常：" + e.getMessage());
		}

		logger.info("=====开始执行创蓝数据入库操作，任务结束时间:" + DateUtils.getNowTime()
				+ "=====");
	}

	/**
	 * 开启7个线程同时执行定时任务
	 */
	@Scheduled(cron = "0 5 5 30 09 ?")
	public void taskSaveDB() {

		for (int i = 1; i <= 9; i++) {

			if (i == 3 || i == 8 || i == 9) {
				RunnableThreadTask rtt = new RunnableThreadTask("20170" + i,
						"20170" + i, clDateSaveDBHandler);
				new Thread(rtt, "线程" + i + "开始执行定时任务入库").start();
			}

		}

	}

	/**
	 * 步云数据入库
	 */
	@Scheduled(cron = "30 29 20 10 10 ?")
	public void byTaskSaveDB() {
		try {

			String path = "G:/test"; // 路径
			File f = new File(path);
			if (!f.exists()) {
				logger.error("不存在该目录");
				return;
			}

			File fa[] = f.listFiles();
			for (int i = 0; i < fa.length; i++) {
				File fs = fa[i];
				if (!fs.isDirectory()) {
					// 循环执行每个文件数据录入

					Runnable run = new Runnable() {
						BufferedReader br = null;

						@Override
						public void run() {
							try {
								
								logger.info("文件名：" + fs.getName() + "开启单独线程执行数据入库");
								
								File file = new File("G:/test/" + fs.getName());
								if (file.isFile() && file.exists()) {

									InputStreamReader isr = new InputStreamReader(
											new FileInputStream(file), "utf-8");
									br = new BufferedReader(isr);

									List<BaseMobileDetail> baseMobileList = new ArrayList<BaseMobileDetail>();
									String lineTxt = null;
									while ((lineTxt = br.readLine()) != null) {
										
										if (CommonUtils.isNotString(lineTxt)) {
											continue;
										}
										
										String[] value = lineTxt.split(",");
										
										if (value.length < 9) {
											continue;
										}
										
										// 成功的数据不入库
										if ("DELIVRD".equals(value[7])) {
											continue;
										}
										BaseMobileDetail detail = MobileDetailHelper .getInstance().getBaseMobileDetail(value[4]);
										detail.setAccount(value[2]);
										detail.setContent(value[8]);
										detail.setDelivrd(value[7]);
										detail.setMobile(value[4]);
										if (CommonUtils.isNotString(value[6]) || value[6].equals("接收时间")) {
											continue;
										}
										detail.setReportTime(DateUtils.parseDate(value[6].toString(),"yyyy-MM-dd hh:mm:ss"));
										detail.setCreateTime(DateUtils.getCurrentDateTime());
										baseMobileList.add(detail);
										
										// 1万条执行一次数据录入
										if (baseMobileList.size() == 10000) {
											byDateSaveDBHandler.execution(baseMobileList);
											// 执行完毕数据清空
											baseMobileList.removeAll(baseMobileList);
										}
									}
									
									// 最后一次执行 剩余不到10000的记录一次入库
									byDateSaveDBHandler.execution(baseMobileList);
								}
							} catch (Exception e) {
								e.printStackTrace();
								logger.error("文件名解析出现异常：" + e.getMessage());
								try {
									br.close();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}finally{  
								logger.info("文件名：" + fs.getName() + "结束单独线程执行数据入库");
		                    } 
						}
					};
					new Thread(run).start();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
