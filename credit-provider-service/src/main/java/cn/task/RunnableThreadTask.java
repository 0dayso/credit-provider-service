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

import com.alibaba.fastjson.JSONObject;

import cn.entity.base.BaseMobileDetail;
import cn.task.handler.ClDateSaveDBHandler;
import cn.task.helper.MobileDetailHelper;
import cn.utils.DateUtils;

public class RunnableThreadTask implements Runnable {

	@Value("${spring.data.elasticsearch.cluster-name}")
	private String clusterName;

	@Value("${spring.data.elasticsearch.cluster-nodes}")
	private String clusterNodes;
	@Value("${spring.data.elasticsearch.cluster-port}")
	private int clusterPort;
	
	private ClDateSaveDBHandler clDateSaveDBHandler;

	private final static Logger logger = LoggerFactory.getLogger(RunnableThreadTask.class);
	
	
	private String startTime;
	
	private String endTime;
	
	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	
	

	public ClDateSaveDBHandler getClDateSaveDBHandler() {
		return clDateSaveDBHandler;
	}

	public void setClDateSaveDBHandler(ClDateSaveDBHandler clDateSaveDBHandler) {
		this.clDateSaveDBHandler = clDateSaveDBHandler;
	}

	@Override
	public void run() {
		
		//	spring.data.elasticsearch.cluster-name=cl-es-cluster
//		spring.data.elasticsearch.cluster-nodes=172.16.20.20
//		spring.data.elasticsearch.cluster-port=9300
		
		logger.info("=====开始执行创蓝数据入库操作，任务开始时间:" + DateUtils.getNowTime() + "=====");

		try {
			Settings settings = Settings.builder().put("cluster.name", "cl-es-cluster").put("client.transport.sniff", true)
					.put("client.transport.ping_timeout", "25s").build();

			@SuppressWarnings("resource")
			TransportClient client = new PreBuiltTransportClient(settings).addTransportAddress(
					new InetSocketTransportAddress(InetAddress.getByName("172.16.20.20"), 9300));

			SearchResponse scrollResp = client.prepareSearch(startTime, endTime).addSort("_doc", SortOrder.ASC)
					.setScroll(new TimeValue(6000)).setSize(100).get();

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
			
			logger.info("=====开始执行创蓝数据入库操作，任务结束时间:" + DateUtils.getNowTime() + "=====");

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("定时任务异常");
		}

	}
	
	public RunnableThreadTask(String startTime,String endTime,ClDateSaveDBHandler clDateSaveDBHandler){
		this.startTime = startTime;
		this.endTime = endTime;
		this.clDateSaveDBHandler = clDateSaveDBHandler;
	}


}
