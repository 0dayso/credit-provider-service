package cn.repository.base;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;

/**
 * 底层BaseRepository 类
 * 
 * @author ChuangLan
 *
 */
public interface BaseMobileDetailRepository<T, ID extends Serializable> {

	/**
	 * 根据手机号码查询
	 * 
	 * @param mobile
	 * @return
	 */
	List<T> findByMobile(String mobile);

	/**
	 * 根据手机号码和时间段查询 默认reportTime降序 
	 * 
	 * @param mobile
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@Query("{ 'mobile' : ?0 , 'reportTime' : { $gte : ?1 , $lte : ?2 }}")
	Page<T> findByMobileAndReportTime(String mobile, Date startTime, Date endTime,Pageable pageable);
	
}
