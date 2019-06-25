package com.yuntongxun.mcm.sevenmoor.model;

import java.util.ArrayList;
import java.util.List;

import com.yuntongxun.mcm.util.JsonUtils;

public class SevenMoorInvestigate {

	private String name;
	private String value;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public static void main(String[] args) {
		TransferData td = new TransferData();
		List<SevenMoorInvestigate> list = new ArrayList<SevenMoorInvestigate>();
		SevenMoorInvestigate s1 = new SevenMoorInvestigate();
		SevenMoorInvestigate s2 = new SevenMoorInvestigate();
		SevenMoorInvestigate s3 = new SevenMoorInvestigate();
		
		s1.setName("满意");
		s1.setValue("7d374a90-ef17-11e5-b85e-47a837a571ce");
		
		s2.setName("非常满意");
		s2.setValue("7d374a91-ef17-11e5-b85e-47a837a571ce");
		
		s3.setName("一般满意");
		s3.setValue("7d374a92-ef17-11e5-b85e-47a837a571ce");

		list.add(s1);
		list.add(s2);
		list.add(s3);
		
		td.setList(list);
		
		System.out.println(JsonUtils.bean2json(td.getList()));
		
		
	}
}
