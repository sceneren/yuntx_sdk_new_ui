package com.yuntongxun.mcm.genesys.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yuntongxun.mcm.util.StringUtil;

public class FTPUtil {

	private Logger logger = LogManager.getLogger(FTPUtil.class);
	
	private String path = "/usr/local/web/apache/htdocs/ceshi/ytx/";
	
	private String tempPath = "F:/temp_files";
	
	private String ftpIp = "192.168.123.195";
	
	private int ftpPort = 21;
	
	private String userName = "root";
	
	private String password = "redhat";
	
	private static FTPClient ftp;

	private FTPUtil() {
	}

	private static FTPUtil ftpUtil;

	public static FTPUtil getInstance() {
		if (ftp == null) {
			ftp = new FTPClient();
		}
		if (ftpUtil == null) {
			ftpUtil = new FTPUtil();
		}
		return ftpUtil;
	}
	
	/**
	 * 连接ftp服务器
	 */
	private void connect() {
		FTPClientConfig ftpClientConfig = new FTPClientConfig(
				FTPClientConfig.SYST_NT);
		ftp.configure(ftpClientConfig);
		try {
			ftp.connect(ftpIp, ftpPort);
			ftp.login(userName, password);
		} catch (IOException e) {
			logger.error("ftpUtil connect error", e.fillInStackTrace());
		}
	}

	/**
	 * 退出关闭连接
	 */
	private void close() {
		if (ftp.isConnected()) {
			try {
				ftp.logout();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					ftp.disconnect();
				} catch (IOException e) {
					logger.error("ftpUtil close error", e.fillInStackTrace());
				}
			}
		}
	}

	public String download(String url, String extendFileName) {
		InputStream is = null;
		String result = null;
		try {
			connect();
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				close();
				return null;
			}
			if(url != null && url.length() != 0){
				is = ftp.retrieveFileStream(url + extendFileName);
			}else{
				is = ftp.retrieveFileStream(path + extendFileName);
			}
			
			if (ftp.completePendingCommand()) {
				logger.info("下载流读取完成");
				
				String fileTempDir = tempPath;
				
				File fileTempPath = new File(fileTempDir);
				if(!fileTempPath.exists()){
					fileTempPath.mkdirs();
				}
				
				//随机生成文件名
				String newFileName = StringUtil.getUUID() + "_"+ extendFileName;
				String filePath = fileTempDir + File.separator + newFileName;
				
				//保存数据到本地
				File file = new File(filePath);
				FileOutputStream out = null;
				try{
					out = new FileOutputStream(file);
					byte[] buffer = new byte[1024*128];
					int c;
					while((c=is.read(buffer,0,buffer.length))!=-1 ){
						out.write(buffer,0,c);
						out.flush();
					}
				}finally{
					if(out!=null){
						out.close();
					}
					if(is!=null){
						is.close();
					}
				}
				
				result = filePath;
			}else{
				logger.info("下载流读取未完成");
			}
		} catch (IOException e) {
			logger.error("download " + url + " faill", e.fillInStackTrace());
		} finally {
			close();
		}
		logger.info("@http download:url: " + url + ", result:" + result + ".");
		
		return result;
	}
	
	/**
	 * 上传
	 * @param filename
	 * @param input
	 * @return
	 */
	public boolean upload(String filename, InputStream input) { 
	    boolean success = false; 
	    try { 
	        connect();
	        
	        int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				close();
				return success;
			}
			
	        ftp.changeWorkingDirectory(path); 
	        ftp.setFileType(ftp.BINARY_FILE_TYPE); //设置上传类型
	        ftp.storeFile(filename, input);          
	       
	        success = true; 
	    } catch (IOException e) { 
	    	logger.error("ftpUtil upload error", e.fillInStackTrace());
	    } finally { 
	        if (ftp.isConnected()) { 
	            try { 
	            	input.close(); 
	    		    ftp.logout(); 
	                ftp.disconnect(); 
	            } catch (IOException ioe) { 
	            } 
	        } 
	    } 
	    return success; 
	}
	
	private final static int BUFFER = 1024;  
	
	public static void main(String[] args) {
		try { 
		     /*  FileInputStream in = new FileInputStream(new File("E:/book.txt")); 
		        boolean flag = FTPUtil.getInstance().upload("book.txt", in); 
		        
		        System.out.println(flag);*/
		        
		   String flag = FTPUtil.getInstance().download("/usr/local/web/apache/htdocs/ceshi/wechat/20160114/", 
		    		 "162243861707.png");
		   System.out.println(flag);
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
		  
		  /*Map<String, InputStream> tempMap = HttpUtil.downloadFileGet("http://192.168.123.206:8888/56001/4028fb4e521d010101521d020bb50001/2016-01-14/19-35/1452771341043887141.jpg");
			for(String key : tempMap.keySet()){
				boolean isFlag = FTPUtil.getInstance().upload(key, tempMap.get(key));
				if(isFlag){
					String returnUrl = "http://192.168.123.195/ceshi/ytx/" + key;
					logger.info("upload[" + returnUrl + "] success.");							    					
				}else{
					logger.info("upload fail.");
				}
			}*/
			
			/*InputStream is = new FileInputStream(new File("F:\\1452771341043887141.jpg"));
			
			boolean isFlag = FTPUtil.getInstance().upload("1452771341043887141.jpg", is);
			System.out.println(isFlag);*/
			
			/*FileOutputStream out = new FileOutputStream(new File("F:\\1452771341043887141.jpg"));  
	      byte[] b = new byte[BUFFER];  
	      int len = 0;  
	      while((len = is.read(b))!= -1){  
	          out.write(b,0,len);  
	      }  
	      is.close();  
	      out.close(); */
	}
	
}
