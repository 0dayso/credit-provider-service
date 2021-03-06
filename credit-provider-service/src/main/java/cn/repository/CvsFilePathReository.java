package cn.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import cn.entity.CvsFilePath;

public interface CvsFilePathReository extends MongoRepository<CvsFilePath, String>{
	
	@Query("{ 'userId' : ?0 ,'isDeleted' : '0'}.sort({'createTime' : -1})")
	List<CvsFilePath> findByUserId(String userId);
	
}
