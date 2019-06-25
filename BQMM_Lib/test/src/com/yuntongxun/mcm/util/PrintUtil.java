package com.yuntongxun.mcm.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PrintUtil {

	public static final Logger logger = LogManager.getLogger(PrintUtil.class);
	
	public static final int MARGIN_0 = 0;
	public static final int MARGIN_1 = 1;
	public static final int MARGIN_2 = 2;
	
	public static void printStartTag(String tagName) {
		printStartTag(tagName, MARGIN_0);
	}

	public static void printStartTag(String tagName, int space) {
		StringBuilder sb = new StringBuilder();
		switch (space) {
		case MARGIN_0:
			break;
		case MARGIN_1:
			sb.append("\r\n");
			break;
		case MARGIN_2:
			sb.append("\r\n\r\n");
			break;
		default:
			sb.append("\r\n");
			break;
		}
		sb.append("----------------------------[").append(tagName).append(" Start").append(
				"]-------------------------------");
		logger.info(sb.toString());
	}

	public static void printEndTag(String tagName) {
		printEndTag(tagName, MARGIN_1);
	}

	public static void printEndTag(String tagName, int space) {
		StringBuilder sb = new StringBuilder("----------------------------[").append(tagName).append(" End").append(
				"]-------------------------------");
		switch (space) {
		case MARGIN_0:
			break;
		case MARGIN_1:
			sb.append("\r\n");
			break;
		case MARGIN_2:
			sb.append("\r\n\r\n");
			break;
		default:
			sb.append("\r\n");
			break;
		}
		logger.info(sb.toString());
	}
}
