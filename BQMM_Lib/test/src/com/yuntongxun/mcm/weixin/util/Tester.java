package com.yuntongxun.mcm.weixin.util;

import org.ming.sample.json.JSONUtil;

import com.yuntongxun.mcm.sevenmoor.model.TransferData;

public class Tester {

	public static void main(String[] args) {
		TransferData td = new TransferData();
		
		String jsonString = "{\"statusCode\":\"sdkLogin\"}";
		
		TransferData object= (TransferData)JSONUtil.jsonToObj(jsonString, TransferData.class);
		System.out.println(object.getStatusCode());

	}

}
