package cn.repository.cm;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.entity.cm.CM150;
import cn.repository.base.BaseMobileDetailRepository;

/**
 * @author ChuangLan
 */
public interface CM150Repository extends MongoRepository<CM150, String> ,BaseMobileDetailRepository<CM150,String>{
    
	
//	Page<CM150> findByMobile(String mobile,Pageable pageable); 
}
