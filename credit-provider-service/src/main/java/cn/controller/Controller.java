package cn.controller;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import cn.entity.base.BaseMobileDetail;
import cn.entity.cm.CM136;
import cn.entity.ct.CT133;
import cn.entity.ct.CT1700;
import cn.service.ForeignService;
import cn.service.cm.CM136Service;
import cn.task.InsertThreadTask;
import cn.task.TodayDataSaveDBTask;
import cn.task.helper.MobileDetailHelper;
import cn.utils.DateUtils;
import cn.utils.UUIDTool;
import main.java.cn.common.BackResult;
import main.java.cn.domain.RunTestDomian;

/**
 * Created by WunHwanTseng on 2016/11/12.
 */
@RestController
public class Controller {

	@Autowired
	private MongoTemplate mongoTemplate;

	// @Autowired
	// private SpaceDetectionService spaceDetectionService;

	@Autowired
	private TodayDataSaveDBTask todayDataSaveDBTask;

	@Autowired
	private ForeignService foreignService;

	@Autowired
	private CM136Service cM136Service;

	private final static Logger logger = LoggerFactory.getLogger(Controller.class);

	@GetMapping("/test")
	public void test() {

		try {
			Settings settings = Settings.builder().put("cluster.name", "cl-es-cluster")
					.put("client.transport.sniff", true).put("client.transport.ping_timeout", "25s").build();

			@SuppressWarnings("resource")
			TransportClient client = new PreBuiltTransportClient(settings)
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("172.16.20.20"), 9300));

			SearchResponse scrollResp = client.prepareSearch("201701", "201701").addSort("_doc", SortOrder.ASC)
					.setScroll(new TimeValue(60000))

					.setSize(100).get(); // max of 100 hits will be returned for
											// each scroll
			int i = 0;
			Map<String, String> map = new HashMap<String, String>();
			do {
				for (SearchHit hit : scrollResp.getHits().getHits()) {
					String json = hit.getSourceAsString();

					System.out.println("i=" + i + ":" + hit.getId() + "," + hit.getSourceAsString());
					JSONObject backjson = (JSONObject) JSONObject.parse(json);

					String account = backjson.getString("account");
					map.put(account, account);

				}

				scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000))
						.execute().actionGet();
			} while (scrollResp.getHits().getHits().length != 0); // Zero hits
																	// mark
																	// the end
																	// of
																	// the
																	// scroll
																	// and the
																	// while
																	// loop.
			for (int k = 0; k < 100000; k++) {
				String kk = map.get(String.valueOf(k));
				if (kk == null) {
					System.out.println(k);
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@GetMapping("/savect133")
	public CT133 savect133() {
		logger.info("1111111111111111");
		CT133 ct = new CT133(UUIDTool.getInstance().getUUID());
		ct.setAccount("M5205956");
		ct.setCity("苏州市");
		ct.setContent("【盟轩】上海3月农化展没订房的展商，展商专享价预订中，展馆附近几公里含早餐班车咨询02131200858企业QQ800067617退订回TD");
		ct.setDelivrd("UNKNOWN");
		ct.setMobile("13362672233");
		ct.setPlatform(1);
		ct.setProductId("productId");
		ct.setProvince("江苏省");
		ct.setReportTime(DateUtils.parseDate("2017-01-05 12:27:23", "yyyy-MM-dd hh:mm:ss"));
		ct.setSignature("盟轩");
		ct.setCreateTime(new Date());
		mongoTemplate.save(ct);

		return ct;
	}

	@GetMapping("/findname")
	public BaseMobileDetail findname() {
		System.out.println(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
		// BaseMobileDetail detail =
		// spaceDetectionService.findByMobile("13663343685");
		List<CM136> detail = cM136Service.findByMobile("13663343685");
		System.out.println(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
		return detail.get(0);
	}

	@GetMapping("/task")
	public void task() {
		todayDataSaveDBTask.ClDateSaveDbTask();
	}

	@GetMapping("/runTheTest")
	public BackResult<RunTestDomian> runTheTest() {
		// System.out.println(new SimpleDateFormat("yyyyMMddHHmmssSSS")
		// .format(new Date() ));
		BackResult<RunTestDomian> result = foreignService.runTheTest("D:/test/mk0001.txt", "1255", "1111111",
				"13817367247");
		// System.out.println(new SimpleDateFormat("yyyyMMddHHmmssSSS")
		// .format(new Date() ));
		return result;
	}

	@Value("${server.port}")
	String port;

	@RequestMapping("/hi")
	public String hi(String name) {
		return "hi " + name + ",i am from port:" + port;
	}

	public static void main1111111111111(String[] args) {

		// 183.194.70.206:59200 172.16.20.20:9300
		try {
			Settings settings = Settings.builder().put("cluster.name", "cl-es-cluster")
					.put("client.transport.sniff", true).put("client.transport.ping_timeout", "25s").build();

			@SuppressWarnings("resource")
			TransportClient client = new PreBuiltTransportClient(settings)
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("172.16.20.20"), 9300));

			QueryBuilder qb = QueryBuilders.termsQuery("mobile", "18232207252");

			Long a = System.currentTimeMillis();
			SearchResponse scrollResp = client.prepareSearch("201704").setQuery(qb).addSort("_doc", SortOrder.ASC)
					.setScroll(new TimeValue(60000))

					.setSize(100).get(); // max of 100 hits will be returned for
											// each scroll
			int i = 0;
			Map<String, String> map = new HashMap<String, String>();
			do {
				for (SearchHit hit : scrollResp.getHits().getHits()) {
					String json = hit.getSourceAsString();

					System.out.println("i=" + i + ":" + hit.getId() + "," + hit.getSourceAsString());
					JSONObject backjson = (JSONObject) JSONObject.parse(json);

					String account = backjson.getString("account");
					map.put(account, account);

				}

				scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000))
						.execute().actionGet();
			} while (scrollResp.getHits().getHits().length != 0); // Zero hits
																	// mark
																	// the end
																	// of
																	// the
																	// scroll
																	// and the
																	// while
																	// loop.
			// for (int k = 0; k < 100000; k++) {
			// String kk = map.get(String.valueOf(k));
			// if (kk == null) {
			// System.out.println(k);
			// }
			// }
			Long b = System.currentTimeMillis();

			System.out.println(b - a);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

//	@GetMapping("/mainabc")
	public void main1(String[] args) {
		try {
			Settings settings = Settings.builder().put("cluster.name", "cl-es-cluster")
					.put("client.transport.sniff", true).put("client.transport.ping_timeout", "25s").build();

			@SuppressWarnings("resource")
			TransportClient client = new PreBuiltTransportClient(settings)
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("172.16.20.20"), 9300));
			
			QueryBuilder qb = QueryBuilders.termsQuery("reportTime", "2017-08-01");

			Long a = System.currentTimeMillis();
			SearchResponse scrollResp = client.prepareSearch("201708").setQuery(qb).addSort("_doc", SortOrder.ASC)
					.setScroll(new TimeValue(60000))

					.setSize(100).get();
			// int i = 0;
			Map<String, String> map = new HashMap<String, String>();

			List<BaseMobileDetail> CT1700list = new ArrayList<BaseMobileDetail>();

			do {
				for (SearchHit hit : scrollResp.getHits().getHits()) {
					String json = hit.getSourceAsString();

					System.out.println(json);
//					JSONObject backjson = (JSONObject) JSONObject.parse(json);
//
//					String mobile = backjson.getString("mobile");
//
//					BaseMobileDetail detail = MobileDetailHelper.getInstance().getBaseMobileDetail(mobile);
//					detail.setAccount(backjson.getString("account"));
//					detail.setCity(backjson.getString("city"));
//					detail.setContent(backjson.getString("content"));
//					detail.setDelivrd(backjson.getString("delivrd"));
//					detail.setMobile(backjson.getString("mobile"));
//					detail.setProductId(backjson.getString("productId"));
//					detail.setProvince(backjson.getString("province"));
//					detail.setReportTime(DateUtils.parseDate(backjson.getString("reportTime"), "yyyy-MM-dd hh:mm:ss"));
//					detail.setSignature(backjson.getString("signature"));
//					detail.setCreateTime(DateUtils.getCurrentDateTime());
//					CT1700list.add(detail);
//					if (CT1700list.size() == 10000) {
//						InsertThreadTask rtt = InsertThreadTask.getInstance(CT1700list, CT1700.class.getName(),
//								mongoTemplate);
//
//						new Thread(rtt, "线程CT1700list开始执行定时任务入库").start();
//						// 清空所有记录
//						CT1700list.removeAll(CT1700list);
//
//						
//
//					}

				

				}

				scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000))
						.execute().actionGet();
			} while (scrollResp.getHits().getHits().length != 0); // Zero hits
																	
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public static void main1111111111111111111111(String[] args) {
		ExecutorService pool = Executors.newCachedThreadPool();
		final Semaphore sp = new Semaphore(10, true);
		for (int i = 0; i < 10; i++) {
			Runnable runnable = new Runnable() {

				@Override
				public void run() {

					try {
						sp.acquire();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println(sp.availablePermits());
					System.out.println(
							"线程  " + Thread.currentThread().getName() + "进入，已有" + (3 - sp.availablePermits()) + "并发");
					try {
						Thread.sleep((long) (Math.random() * 3000));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println("线程  " + Thread.currentThread().getName() + "即将离开 ");
					sp.release();
					System.out.println(
							"线程  " + Thread.currentThread().getName() + "离开 ，已有" + (3 - sp.availablePermits()) + "并发");
				}
			};
			pool.execute(runnable);
		}
	}

	public static void main1111111(String[] args) {
		// 183.194.70.206:59200 172.16.20.20:9300
		try {
			Settings settings = Settings.builder().put("cluster.name", "cl-es-cluster")
					.put("client.transport.sniff", true).put("client.transport.ping_timeout", "25s").build();

			@SuppressWarnings("resource")
			TransportClient client = new PreBuiltTransportClient(settings)
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("172.16.20.20"), 9300));

			QueryBuilder qb = QueryBuilders.termsQuery("reportTime", "2017-07-01");

			Long a = System.currentTimeMillis();
			SearchResponse scrollResp = client.prepareSearch("201707").setQuery(qb).addSort("_doc", SortOrder.ASC)
					.setScroll(new TimeValue(60000))

					.setSize(100).get(); // max of 100 hits will be returned for
											// each scroll
			int i = 0;
			Map<String, String> map = new HashMap<String, String>();
			do {
				for (SearchHit hit : scrollResp.getHits().getHits()) {
					String json = hit.getSourceAsString();

					System.out.println("i=" + i + ":" + hit.getId() + "," + hit.getSourceAsString());
					JSONObject backjson = (JSONObject) JSONObject.parse(json);

					String account = backjson.getString("account");
					map.put(account, account);

				}

				scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000))
						.execute().actionGet();
			} while (scrollResp.getHits().getHits().length != 0); // Zero hits
																	// mark
																	// the end
																	// of
																	// the
																	// scroll
																	// and the
																	// while
																	// loop.
			// for (int k = 0; k < 100000; k++) {
			// String kk = map.get(String.valueOf(k));
			// if (kk == null) {
			// System.out.println(k);
			// }
			// }
			Long b = System.currentTimeMillis();

			System.out.println(b - a);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	public static void main(String[] args) {
		try {
			Settings settings = Settings.builder().put("cluster.name", "cl-es-cluster")
					.put("client.transport.sniff", true).put("client.transport.ping_timeout", "25s").build();

			@SuppressWarnings("resource")
			TransportClient client = new PreBuiltTransportClient(settings)
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("172.16.20.20"), 9300));
			
			QueryBuilder qb = QueryBuilders.termsQuery("reportTime", "2017-09-30");

			Long a = System.currentTimeMillis();
			SearchResponse scrollResp = client.prepareSearch("201709").setQuery(qb).addSort("_doc", SortOrder.ASC)
					.setScroll(new TimeValue(60000))

					.setSize(100).get();
			// int i = 0;
			Map<String, String> map = new HashMap<String, String>();

			List<BaseMobileDetail> CT1700list = new ArrayList<BaseMobileDetail>();

			do {
				for (SearchHit hit : scrollResp.getHits().getHits()) {
					String json = hit.getSourceAsString();

					System.out.println(json);
//					JSONObject backjson = (JSONObject) JSONObject.parse(json);
//
//					String mobile = backjson.getString("mobile");
//
//					BaseMobileDetail detail = MobileDetailHelper.getInstance().getBaseMobileDetail(mobile);
//					detail.setAccount(backjson.getString("account"));
//					detail.setCity(backjson.getString("city"));
//					detail.setContent(backjson.getString("content"));
//					detail.setDelivrd(backjson.getString("delivrd"));
//					detail.setMobile(backjson.getString("mobile"));
//					detail.setProductId(backjson.getString("productId"));
//					detail.setProvince(backjson.getString("province"));
//					detail.setReportTime(DateUtils.parseDate(backjson.getString("reportTime"), "yyyy-MM-dd hh:mm:ss"));
//					detail.setSignature(backjson.getString("signature"));
//					detail.setCreateTime(DateUtils.getCurrentDateTime());
//					CT1700list.add(detail);
//					if (CT1700list.size() == 10000) {
//						InsertThreadTask rtt = InsertThreadTask.getInstance(CT1700list, CT1700.class.getName(),
//								mongoTemplate);
//
//						new Thread(rtt, "线程CT1700list开始执行定时任务入库").start();
//						// 清空所有记录
//						CT1700list.removeAll(CT1700list);
//
//						
//
//					}

				

				}

				scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000))
						.execute().actionGet();
			} while (scrollResp.getHits().getHits().length != 0); // Zero hits
																	
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
