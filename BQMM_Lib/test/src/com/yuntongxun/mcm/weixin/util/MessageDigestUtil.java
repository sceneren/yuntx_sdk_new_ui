package com.yuntongxun.mcm.weixin.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import com.yuntongxun.mcm.util.MD5Util;

public class MessageDigestUtil {
	
	public static String sha1(String str) throws NoSuchAlgorithmException{
		MessageDigest md = MessageDigest.getInstance("SHA");
		md.update(str.getBytes());
		String result = Hex.encodeHexString(md.digest());
		return result;
	}
	
	public static void main(String[] agrs) throws NoSuchAlgorithmException{
		String sha1 = MessageDigestUtil.sha1("weily");
		String md5 = MessageDigestUtil.md5("weily");
		System.out.println("sha1:"+sha1);
		System.out.println("md5:"+md5);
		System.out.println("md5:"+MD5Util.string2MD5("weily"));
	}
	
	public static String md5(String str) throws NoSuchAlgorithmException{
		MessageDigest md = MessageDigest.getInstance("md5");
		md.update(str.getBytes());
		String result = Hex.encodeHexString(md.digest());
		return result;
	}

}
