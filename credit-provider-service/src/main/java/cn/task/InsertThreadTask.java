package cn.task;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;

import cn.entity.base.BaseMobileDetail;

public class InsertThreadTask implements Runnable {
	
	
	private static InsertThreadTask uuidtool;
	
	public static InsertThreadTask getInstance(List<BaseMobileDetail> list,String collectionName,MongoTemplate mongoTemplate) {  
        if (uuidtool == null) {    
            synchronized (InsertThreadTask.class) {    
               if (uuidtool == null) {    
            	   uuidtool = new InsertThreadTask(list,collectionName,mongoTemplate);   
               }    
            }    
        }    
        return uuidtool;   
    }  
	
	

    private MongoTemplate mongoTemplate;
    
    public MongoTemplate getMongoTemplate() {
		return mongoTemplate;
	}

	public void setMongoTemplate(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	private String collectionName;
    
	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	private List<BaseMobileDetail> list;
	
	public List<BaseMobileDetail> getList() {
		return list;
	}

	public void setList(List<BaseMobileDetail> list) {
		this.list = list;
	}

	@Override
	public void run() {
		try {
		  	mongoTemplate.insertAll(list);
//			mongoTemplate.save(list, collectionName);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public InsertThreadTask(List<BaseMobileDetail> list,String collectionName,MongoTemplate mongoTemplate){
		this.list = list;
		this.collectionName = collectionName;
		this.mongoTemplate = mongoTemplate;
	}


}
