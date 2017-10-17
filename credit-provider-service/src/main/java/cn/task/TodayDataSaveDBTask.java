package cn.task;

import java.net.InetAddress;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import cn.entity.base.BaseMobileDetail;
import cn.entity.ct.CT1700;
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

	@Autowired
	private MongoTemplate mongoTemplate;

	private final static Logger logger = LoggerFactory.getLogger(TodayDataSaveDBTask.class);

	// 该任务执行一次 时间 秒 分 时 天 月 年
	@Scheduled(cron = "30 15 10 4 10 ?")
	public void ClDateSaveDbTask() {
		logger.info("=====开始执行创蓝数据入库操作，任务开始时间:" + DateUtils.getNowTime() + "=====");

		try {
			Settings settings = Settings.builder().put("cluster.name", clusterName).put("client.transport.sniff", true)
					.put("client.transport.ping_timeout", "125s").build();

			@SuppressWarnings("resource")
			TransportClient client = new PreBuiltTransportClient(settings).addTransportAddress(
					new InetSocketTransportAddress(InetAddress.getByName(clusterNodes), clusterPort));

			SearchResponse scrollResp = client.prepareSearch("201701").addSort("_doc", SortOrder.ASC)
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
	// @Scheduled(cron = "30 10 18 4 10 ?")
	public void taskSaveDB() {

		for (int i = 1; i <= 9; i++) {

			RunnableThreadTask rtt = new RunnableThreadTask("20170" + i, "20170" + i, clDateSaveDBHandler);
			new Thread(rtt, "线程" + i + "开始执行定时任务入库").start();

		}

	}

	

	// 该任务执行一次 时间 秒 分 时 天 月 年
	@Scheduled(cron = "20 8 16 8 10 ?")
	public void insertMongodb() {

		try {
			Settings settings = Settings.builder().put("cluster.name", "cl-es-cluster")
					.put("client.transport.sniff", true).put("client.transport.ping_timeout", "25s").build();

			@SuppressWarnings("resource")
			TransportClient client = new PreBuiltTransportClient(settings)
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("172.16.20.20"), 9300));

			Calendar calendar = Calendar.getInstance();
			int year = 2017;
			int month = 0;
			int date = 1;
			calendar.set(year, month, date);
			int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			int minDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);

			for (int i = minDay; i <= maxDay; i++) {
				calendar.set(year, month, i);

				logger.info("开始执行" + DateUtils.formatDate(calendar.getTime(), "yyyy-MM-dd") + "的数据");

				QueryBuilder qb = QueryBuilders.termsQuery("reportTime",
						DateUtils.formatDate(calendar.getTime(), "yyyy-MM-dd"));
				SearchResponse scrollResp = client.prepareSearch(DateUtils.formatDate(calendar.getTime(), "yyyyMM"))
						.setQuery(qb).addSort("_doc", SortOrder.ASC).setScroll(new TimeValue(60000))

						.setSize(100).get();

				List<BaseMobileDetail> baseList = new ArrayList<BaseMobileDetail>();

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
						detail.setReportTime(
								DateUtils.parseDate(backjson.getString("reportTime"), "yyyy-MM-dd hh:mm:ss"));
						detail.setSignature(backjson.getString("signature"));
						detail.setCreateTime(DateUtils.getCurrentDateTime());
						baseList.add(detail);
						if (baseList.size() == 10000) {
							InsertThreadTask rtt = InsertThreadTask.getInstance(baseList, CT1700.class.getName(),
									mongoTemplate);
							new Thread(rtt, "线程CT1700list开始执行定时任务入库").start();
							// 清空所有记录
							baseList.remove(baseList);
						}
					}

					scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000))
							.execute().actionGet();
				} while (scrollResp.getHits().getHits().length != 0);

				logger.info("结束执行" + DateUtils.formatDate(calendar.getTime(), "yyyy-MM-dd") + "的数据");

			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("=====执行创蓝数据入库出现异常：" + e.getMessage());
		}

	}
	
	
	// 该任务执行一次 时间 秒 分 时 天 月 年
		public void insertMongodbfor(String strdate) {

			try {
				Settings settings = Settings.builder().put("cluster.name", "cl-es-cluster")
						.put("client.transport.sniff", true).put("client.transport.ping_timeout", "25s").build();

				@SuppressWarnings("resource")
				TransportClient client = new PreBuiltTransportClient(settings)
						.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("172.16.20.20"), 9300));
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						
						QueryBuilder qb = QueryBuilders.termsQuery("reportTime", strdate);

						Long a = System.currentTimeMillis();
						SearchResponse scrollResp = client.prepareSearch("201709").setQuery(qb).addSort("_doc", SortOrder.ASC)
								.setScroll(new TimeValue(60000))

								.setSize(100).get();

						List<BaseMobileDetail> baseList = new ArrayList<BaseMobileDetail>();

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
								detail.setReportTime(
										DateUtils.parseDate(backjson.getString("reportTime"), "yyyy-MM-dd hh:mm:ss"));
								detail.setSignature(backjson.getString("signature"));
								try {
									detail.setCreateTime(DateUtils.getCurrentDateTime());
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								mongoTemplate.save(detail);
							}

							scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000))
									.execute().actionGet();
						} while (scrollResp.getHits().getHits().length != 0);
						
						mongoTemplate.insertAll(baseList);
						
					}
				}, strdate+"线程开始执行定时任务入库").start();

			} catch (Exception e) {
				e.printStackTrace();
				logger.error("=====执行创蓝数据入库出现异常：" + e.getMessage());
			} 

		}
		
		/**
		 * 执行9月 天天数据入库
		 */
		@Scheduled(cron = "0 26 18 16 10 ?")
		public void insertMongodbfor(){
			String[] strDate = {"2017-09-19","2017-09-18","2017-09-17","2017-09-16","2017-09-15","2017-09-14","2017-09-13","2017-09-12","2017-09-12","2017-09-11","2017-09-10"};
			
//			String[] strDate = {"2017-09-30"};
			
			for (int i = 0; i < strDate.length; i++) {
				this.insertMongodbfor(strDate[i]);
			}
			
		}
		
		public static void main(String[] args) {

			String[] strDate = {"2017-09-30","2017-09-28","2017-09-27","2017-09-26","2017-09-25","2017-09-24","2017-09-23","2017-09-22","2017-09-22","2017-09-21","2017-09-20"};
			
			for (int i = 0; i < strDate.length; i++) {
				TodayDataSaveDBTask task = new TodayDataSaveDBTask();
				task.insertMongodbfor(strDate[i]);
			}

		}
	// 该任务执行一次 时间 秒 分 时 天 月 年
//	@Scheduled(cron = "0 0 22 9 10 ?")
	public void main111() {
		try {
			Settings settings = Settings.builder().put("cluster.name", "cl-es-cluster")
					.put("client.transport.sniff", true).put("client.transport.ping_timeout", "25s").build();

			@SuppressWarnings("resource")
			TransportClient client = new PreBuiltTransportClient(settings)
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("172.16.20.20"), 9300));

			Calendar calendar = Calendar.getInstance();
			int year = 2017;
			int month = 0;
			int date = 1;
			calendar.set(year, month, date);
			int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			int minDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
			
			for (int i = minDay; i <= maxDay; i++) {
				calendar.set(year, month, i);
				
				QueryBuilder qb = QueryBuilders.termsQuery("reportTime", DateUtils.formatDate(calendar.getTime(), "yyyy-MM-dd"));

				SearchResponse scrollResp = client.prepareSearch(DateUtils.formatDate(calendar.getTime(), "yyyyMM")).setQuery(qb).addSort("_doc", SortOrder.ASC)
						.setScroll(new TimeValue(60000))

						.setSize(100).get();

				List<BaseMobileDetail> CT1700list = new ArrayList<BaseMobileDetail>();

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
						CT1700list.add(detail);
						if (CT1700list.size() == 10000) {
							InsertThreadTask rtt = InsertThreadTask.getInstance(CT1700list, CT1700.class.getName(),
									mongoTemplate);
							new Thread(rtt, "线程CT1700list开始执行定时任务入库").start();
							// 清空所有记录
							CT1700list.removeAll(CT1700list);
						}

					}

					scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000))
							.execute().actionGet();
				} while (scrollResp.getHits().getHits().length != 0); // Zero hits
				
				// 清空所有记录
				CT1700list.removeAll(CT1700list);
				
				logger.info("结束执行" + DateUtils.formatDate(calendar.getTime(), "yyyy-MM-dd") + "的数据");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("=====执行创蓝数据入库出现异常：" + e.getMessage());
		}
	}

}
