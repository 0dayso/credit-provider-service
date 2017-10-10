package cn.utils;

import java.util.UUID;

public class UUIDTool {
	
	
	private static UUIDTool uuidtool;
	
	public static UUIDTool getInstance() {  
        if (uuidtool == null) {    
            synchronized (UUIDTool.class) {    
               if (uuidtool == null) {    
            	   uuidtool = new UUIDTool();   
               }    
            }    
        }    
        return uuidtool;   
    }  
	
	/**  
     * 自动生成32位的UUid，对应数据库的主键id进行插入用。  
     * @return  
     */  
    public String getUUID() {    
        return UUID.randomUUID().toString().replace("-", "");  
    }  
    
    public static void main(String[] args) {
		System.out.println(UUIDTool.getInstance().getUUID());
	}
}
