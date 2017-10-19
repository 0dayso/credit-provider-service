package cn.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import cn.entity.MobileNumberSection;

public interface MobileNumberSectionReository extends MongoRepository<MobileNumberSection, String>{
	
	@Query("{ 'numberSection' : ?0 }")
	List<MobileNumberSection> findByNumberSection(String numberSection);

}
