package com.yuntongxun.mcm.util;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class StringUtil {

	public static final String allChar = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	public static int incrementVar = 0;

	/**
	 * 得到固定的IP从/127.0.0.1:78658
	 * 
	 * @param remoteHost
	 * @return
	 */
	public static String getRemoteHost(String remoteHost) {
		return remoteHost.substring(1, remoteHost.indexOf(":"));
	}

	public static String getFileNameSuffix(String fileName) {
		if (fileName == null || fileName.lastIndexOf(".") == -1) {
			return "";
		}
		int pos = fileName.lastIndexOf(".");
		return fileName.substring(pos);
	}

	/**
	 * 返回一个定长的随机字符串(只包含大小写字母、数字)
	 * 
	 * @param length
	 *            随机字符串长度
	 * @return 随机字符串
	 */
	public static String generateString(int length) {
		StringBuffer sb = new StringBuffer();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			sb.append(allChar.charAt(random.nextInt(allChar.length())));
		}
		return sb.toString();
	}

	/**
	 * 替换SQL注入的单引号（'）
	 * 
	 * @param str
	 * @return
	 */
	public static String replaceSingleQuote(String str) {
		if (str != null && !"".equals(str)) {
			StringBuffer result = new StringBuffer();
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				if (c == '\'') {
					result.append("");
				} else {
					result.append(str.charAt(i));
				}
			}
			return result.toString();
		} else {
			return "";
		}
	}

	/**
	 * 根据位数生成随机数
	 * 
	 * @param number
	 *            位数
	 * @return n 随机数
	 */
	public static String generateRandomNum(int number) {
		StringBuffer num = new StringBuffer();
		while (number > 0) {
			num.append(String.valueOf(new Random().nextInt(10)));// 获取大于等于0，小于10的整型随机数
			number--;
		}
		String n = num.toString();
		return n;
	}

	/**
	 * 将空串转为0
	 * 
	 * @param s
	 *            空串(null或者"")
	 * @return
	 */
	public static String NullToZero(String s) {
		if (s == null || s.trim().length() == 0) {
			s = "0";
		}
		return s;
	}

	/**
	 * 根据传参number，生成12位字符串，位数不够在number前补零
	 * 
	 * @param number
	 *            数字
	 * @return 12位字符串
	 */
	public static String generateTwelveString(BigDecimal number) {
		String s = "";
		if (number != null) {
			s = number.toString();
			if (s.length() == 1) {
				s = "00000000000" + s;
			} else if (s.length() == 2) {
				s = "0000000000" + s;
			} else if (s.length() == 3) {
				s = "000000000" + s;
			} else if (s.length() == 4) {
				s = "00000000" + s;
			} else if (s.length() == 5) {
				s = "0000000" + s;
			} else if (s.length() == 6) {
				s = "000000" + s;
			} else if (s.length() == 7) {
				s = "00000" + s;
			} else if (s.length() == 8) {
				s = "0000" + s;
			} else if (s.length() == 9) {
				s = "000" + s;
			} else if (s.length() == 10) {
				s = "00" + s;
			} else if (s.length() == 11) {
				s = "0" + s;
			} else {
				s = "" + s;
			}
		}
		return s;
	}

	/**
	 * 根据传参number，生成8位字符串，位数不够在number前补零
	 * 
	 * @param number
	 *            数字
	 * @return 8位字符串
	 */
	public static String generateEightString(BigDecimal number) {
		String s = "";
		if (number != null) {
			s = number.toString();
			if (s.length() == 1) {
				s = "0000000" + s;
			} else if (s.length() == 2) {
				s = "000000" + s;
			} else if (s.length() == 3) {
				s = "00000" + s;
			} else if (s.length() == 4) {
				s = "0000" + s;
			} else if (s.length() == 5) {
				s = "000" + s;
			} else if (s.length() == 6) {
				s = "00" + s;
			} else if (s.length() == 7) {
				s = "0" + s;
			} else {
				s = "" + s;
			}
		}
		return s;
	}

	/**
	 * 生成32位随机字符串
	 * 
	 * @return
	 */
	public static String getUUID() {
		return UUID.randomUUID().toString();
	}
	
	/**
	 * 计算字符长度
	 * 
	 * @param value
	 * @return
	 */
	public static int length(String value) {
		int valueLength = 0;
		String chinese = "[\u0391-\uFFE5]";
		// 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1
		for (int i = 0; i < value.length(); i++) {
			// 获取一个字符
			String temp = value.substring(i, i + 1);
			// 判断是否为中文字符
			if (temp.matches(chinese)) {
				// 中文字符长度为2
				valueLength += 2;
			} else {
				// 其他字符长度为1
				valueLength += 1;
			}
		}
		return valueLength;
	}

	/**
	 * 从后往前截取字符串
	 * 
	 * @param src
	 *            被截取的字符串
	 * @param end
	 *            从后往前截取的位数
	 * @return
	 */
	public static String subEndStr(String src, int end) {
		StringBuffer des1 = new StringBuffer("");
		StringBuffer des = new StringBuffer("");
		char c = '0';
		if (src != null && src.trim().length() > 0) {
			int i = src.length() - 1;
			while (i >= 0) {
				c = src.charAt(i);
				des1.append(c);
				i--;
			}
			String s = des1.toString();
			String ss = s.substring(0, end);
			if (ss != null && ss.trim().length() > 0) {
				int j = ss.length() - 1;
				while (j >= 0) {
					c = ss.charAt(j);
					des.append(c);
					j--;
				}
			}
		}
		return des.toString();
	}

	/**
	 * 获取语音文件串
	 * 
	 * @param prefix
	 *            语音文件路径
	 * @param suffix
	 *            语音文件后缀
	 * @param split
	 *            语音文件分隔符
	 * @param voiceCode
	 *            语音验证码
	 * @return 语音验证码文件串
	 */
	public static String getVoiceCode(String prefix, String suffix, String split, String voiceCode) {
		StringBuffer vcode = new StringBuffer();
		String tips = "tishiyin";
		vcode.append(prefix).append(tips).append(suffix).append(split);
		if (voiceCode != null && voiceCode.trim().length() > 0) {
//			System.out.println("before lower case VoiceCode = " + voiceCode);
			voiceCode = voiceCode.toLowerCase();
//			System.out.println("after lower case VoiceCode = " + voiceCode);
			int len = voiceCode.length();
			for (int i = 0; i < len; i++) {
				char c = voiceCode.charAt(i);
				if (i == len - 1) {
					vcode.append(prefix).append(c).append(suffix);
				} else {
					vcode.append(prefix).append(c).append(suffix).append(split);
				}
			}
//			System.out.println("VoiceCode = " + vcode.toString());
		}
		return vcode.toString();
	}

	public static String getFSIndex(String index) {
		String str = "";
		int length = index.length();
		switch (length) {
		case 1:
			str = "0000" + index;
			break;
		case 2:
			str = "000" + index;
			break;
		case 3:
			str = "00" + index;
			break;
		case 4:
			str = "0" + index;
			break;
		case 5:
			str = index;
		}
		return str;
	}

	/**
	 * json兼容 判断json是否以[开头 ]结尾
	 */
	public static String checkJson(String json) {
		return json.startsWith("[") && json.endsWith("]") ? json : json.startsWith("[") && !json.endsWith("]") ? json
				+ "]" : !json.startsWith("[") && json.endsWith("]") ? "[" + json : "[" + json + "]";
	}

	/**
	 * 解析from to
	 */
	public static String getFromOrTo(String str) {
		return str.substring(str.indexOf(":") + 1, str.lastIndexOf("@")).toLowerCase();
	}

	/**
	 * 判断数据长度是否超过有效长度
	 */
	public static boolean isTooLong(String[] args, int[] lens) {
		boolean flag = false;
		int argLen = args.length;
		if (args.length > lens.length) {
			argLen = lens.length;
		} else if (args.length < lens.length) {
			argLen = args.length;
		}
		for (int i = 0; i < argLen; i++) {
			if (args[i] != null) {
				if (length(args[i]) > lens[i]) {
					flag = true;
					break;
				}
			}
		}
		return flag;
	}

	/**
	 * 去掉换行符
	 * 
	 * @param str
	 * @return
	 */
	public static String replaceBlank(String str) {
		String dest = "";
		if (str != null) {
			Pattern p = Pattern.compile("\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest;
	}

	/**
	 * 添加JSON换行符
	 * 
	 * @param str
	 * @return
	 */
	public static String addLineFeed(String str) {
		String dest = "";
		if (str != null) {
			Pattern p = Pattern.compile("\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest;
	}

	/**
	 * 判断是否为数字
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		if (str == null || str.equals("")) {
			return false;
		}
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}

	/**
	 * 判断是否为浮点数，包括double和float
	 * 
	 * @param str
	 *            传入的字符串
	 * @return 是浮点数返回true,否则返回false
	 */
	public static boolean isDouble(String str) {
		Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
		return pattern.matcher(str).matches();
	}

	/**
	 * 判断list中字符串是否被指定的字串包含
	 * 
	 * @param str
	 * @param list
	 * @return
	 */
	public static boolean checkInStr(String str, List<String> list) {
		for (String l : list) {
			if (str.indexOf(l) != -1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 字符串数组转字符串
	 * 
	 * @param sep
	 * @param list
	 * @return
	 */
	public static String StringListToString(List<String> list, String sep) {
		String str = null;
		if (list != null && list.size() > 0) {
			str = list.get(0);
			for (int i = 1; i < list.size(); i++) {
				String tmp = sep + list.get(i);
				str = str + tmp;
			}
		}
		return str;
	}

	/**
	 * 判断是否为数字或字母
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumericAndABC(String str) {
		if (str == null || str.equals("")) {
			return false;
		}
		Pattern pattern = Pattern.compile("[A-Z,a-z,0-9]*");
		return pattern.matcher(str).matches();
	}

	/**
	 * 判断是否为正确的电话号码
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isPhoneNum(String str) {
		if (str == null || str.equals("")) {
			return false;
		}
		Pattern pattern = Pattern.compile("[0-9,#,*,-]*");
		return pattern.matcher(str).matches();
	}

	/**
	 * 判断strs数组是否包含list集合外的字符串
	 * 
	 * @param list
	 * @param strs
	 * @return
	 */
	public static boolean checkInStr(List<String> list, String[] strs) {
		for (String str : strs) {
			if (!list.contains(str)) {
				return false;
			}
		}
		return true;
	}
	
	public static String getUserAcc(String appId, String userAccount){
		StringBuilder builder = new StringBuilder();
		builder.append(appId);
		builder.append("#");
		builder.append(userAccount);
		return builder.toString();
	}
	
	/**
	 * @Description: 根据userAcc获取appId
	 * @param @param userAcc
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String getAppIdFormUserAcc(String userAcc){
		if (StringUtils.isBlank(userAcc)){
			return "";
		}
		String[] strs = userAcc.split("#");
		if(strs != null && strs.length>1){
			return strs[0];
		}else{
			return "";
		}
	}
	
	/**
	 * @Description: 根据userAcc获取userName
	 * @param @param userAcc
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String getUserNameFormUserAcc(String userAcc){
		if (StringUtils.isBlank(userAcc)){
			return "";
		}
		String[] strs = userAcc.split("#");
		if(strs != null && strs.length>1){
			return strs[1];
		}else{
			return userAcc;
		}
	}
	
	public static Map<String, String> splitUserAcc(String userAcc){
		Map<String, String> map = new HashMap<String, String>();
		String[] strs = userAcc.split("#");
		if(strs != null && strs.length>1){
			map.put("appId", strs[0]);
			map.put("userAccount", strs[1]);
		}
		return map;
	} 
	
	/**
	 * 
	 * 生成msssageId
	 * //13位日期时间串+3位随机数+8位模块编号+8位随机sessionId
	 * *****/
	public static  String generateMessageMsgId(Long current,String moduleCode) {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer();
		//13位时间串
		sb.append(current);
		//3位随机数
		sb.append(StringUtil.generateString(Constants.MESSAGE_ID_RANDOM_LENGTH));
		//8位模块编号
		sb.append(moduleCode);
		//8位随机sessionID
		sb.append(StringUtil.generateRandomNum(Constants.SESSION_ID_LENGTH));
		return sb.toString();
	}
	
	/**
	 * 
	 * 生成msssageId
	 * //13位日期时间串+3位随机数+8位模块编号+8位随机sessionId（已过时）
	 * 10位时间串+6位循环递增数+8位模块编号+8位conntorId
	 * *****/
	public static String generateMessageMsgId(int moduleCode,String conntorId) {
		DateFormat df = new SimpleDateFormat("MMddhhmmss");
		String nowDate = df.format(new Date());
		StringBuffer sb = new StringBuffer();
		//10位时间串
		sb.append(nowDate);
		//6位自增数
		sb.append(zerosDigit(String.valueOf(getIncrementVar()),6));
		//8位模块编号
		sb.append(zerosDigit(String.valueOf(moduleCode),8));
		//8位connectorId
		sb.append(zerosDigit(conntorId,8));
		return sb.toString();
	}
	
	/**
	 * 
	 * 生成msssageId
	 * //13位日期时间串+3位随机数+8位模块编号+8位随机sessionId（已过时）
	 * 10位时间串+6位循环递增数+8位模块编号+8位conntorId
	 * *****/
	public static String generateMessageMsgId(String moduleCode,String conntorId) {
		DateFormat df = new SimpleDateFormat("MMddhhmmss");
		String nowDate = df.format(new Date());
		StringBuffer sb = new StringBuffer();
		//10位时间串
		sb.append(nowDate);
		//6位自增数
		sb.append(zerosDigit(String.valueOf(getIncrementVar()),6));
		//8位模块编号
		sb.append(zerosDigit(String.valueOf(moduleCode),8));
		//8位connectorId
		sb.append(zerosDigit(conntorId,8));
		return sb.toString();
	}
	
	/**
	 * msgId 6位自增数（1-999999）
	 * @return
	 */
	public static synchronized int getIncrementVar(){
		if(incrementVar<999999){
			incrementVar++;
		}else{
			incrementVar = 1;
		}
		return incrementVar;
	}
	
	/**
	 * 对目标数字补零
	 * @param number 目标数字
	 * @param digit 补够位数
	 * @return 
	 */
	public static String zerosDigit(String number,int digit){
		String result = null;
		result = String.valueOf(number);
		if(result.length()<digit){
			int needZeroCount = digit-result.length();
			StringBuffer zeroBuf = new StringBuffer();
			for(int i=0;i<needZeroCount;i++){
				zeroBuf.append("0");
			}
			result = zeroBuf.append(number).toString();
		}
		return result;
	}
	
	/**
	* @Description: 会话ID 
	* @throws
	 */
	public static String generateSid(int moduleCode, String conntorId) {
		DateFormat df = new SimpleDateFormat("MMddhhmmss");
		String nowDate = df.format(new Date());
		StringBuffer sb = new StringBuffer();
		//10位时间串
		sb.append(nowDate);
		//6位自增数
		sb.append(zerosDigit(String.valueOf(getIncrementVar()),6));
		//8位模块编号
		sb.append(zerosDigit(String.valueOf(moduleCode),8));
		//8位connectorId
		sb.append(zerosDigit(conntorId,8));
		return sb.toString();
	}
	
	/**
	 * @Description: 响应模板
	 * @param code
	 * @param msg 
	 */
	public static String getHttpResponse(String code, String msg) {
		String template = "{\"statusCode\":\"%s\",\"statusMsg\":\"%s\"}";
		if(StringUtils.isBlank(msg)){
			msg = "";
		}
		return String.format(template, code, msg);
	}
	
	public static void main(String[] args) {
		System.out.println(StringUtil.getUUID());
	}
}
