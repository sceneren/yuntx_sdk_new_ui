package com.yuntongxun.mcm.model;

/**
 * Created by weily
 */
public class MutiMsgFile {
    private String fileName;
    private String url;

    public MutiMsgFile(String fileName,String url){
        this.fileName = fileName;
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}
    
}
