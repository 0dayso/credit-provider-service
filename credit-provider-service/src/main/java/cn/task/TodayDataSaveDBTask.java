package cn.task;

import java.net.InetAddress;

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
import cn.task.handler.ClDateSaveDBHandler;
import cn.task.helper.MobileDetailHelper;
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

	private final static Logger logger = LoggerFactory.getLogger(TodayDataSaveDBTask.class);

	// 该任务执行一次 时间 秒 分 时 天 月 年
	@Scheduled(cron = "0 39 15 28 09 ?")
	public void ClDateSaveDbTask() {
		logger.info("=====开始执行创蓝数据入库操作，任务开始时间:" + DateUtils.getNowTime() + "=====");

		try {
			Settings settings = Settings.builder().put("cluster.name", clusterName).put("client.transport.sniff", true)
					.put("client.transport.ping_timeout", "25s").build();

			@SuppressWarnings("resource")
			TransportClient client = new PreBuiltTransportClient(settings).addTransportAddress(
					new InetSocketTransportAddress(InetAddress.getByName(clusterNodes), clusterPort));

			SearchResponse scrollResp = client.prepareSearch("201701", "201701").addSort("_doc", SortOrder.ASC)
					.setScroll(new TimeValue(60000)).setSize(100).get();

			do {
				for (SearchHit hit : scrollResp.getHits().getHits()) {

					String json = hit.getSourceAsString();

					JSONObject backjson = (JSONObject) JSONObject.parse(json);

					String mobile = backjson.getString("mobile");

					BaseMobileDetail detail = MobileDetailHelper.getInstance().getBaseMobileDetail(mobile);
					detail.setAccount(backjson.getString("account"));
					detail.setCity(backjson.getString("city"));
					detail.setContent(backjson.getString("content"));
					detail.setDelivrd(backjson.getString("delivrd"));
					detail.setMobile(backjson.getString("mobile"));
					detail.setProductId(backjson.getString("productId"));
					detail.setProvince(backjson.getString("province"));
					detail.setReportTime(DateUtils.parseDate(backjson.getString("reportTime"), "yyyy-MM-dd hh:mm:ss"));
					detail.setSignature(backjson.getString("signature"));
					detail.setCreateTime(DateUtils.getCurrentDateTime());
					clDateSaveDBHandler.execution(detail);
				}

				scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000))
						.execute().actionGet();
			} while (scrollResp.getHits().getHits().length != 0);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("=====执行创蓝数据入库出现异常：" + e.getMessage());
		}

		logger.info("=====开始执行创蓝数据入库操作，任务结束时间:" + DateUtils.getNowTime() + "=====");
	}

	/**
	 * 开启7个线程同时执行定时任务
	 */
	@Scheduled(cron = "30 50 22 3 10 ?")
	public void taskSaveDB() {

		for (int i = 1; i <= 1; i++) {
			
			RunnableThreadTask rtt = new RunnableThreadTask("20170" + i, "20170" + i,clDateSaveDBHandler);
			new Thread(rtt, "线程" + i + "开始执行定时任务入库").start();
			
		}

	}
	
	public static void main(String[] args) {
		
//		for (int i = 1; i <= 7; i++) {
//			RunnableThreadTask rtt = new RunnableThreadTask("20170" + i, "20170" + i);
//			new Thread(rtt, "线程" + i + "开始执行定时任务入库").start();
//		}
		
		
	}

}
