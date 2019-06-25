package com.yuntongxun.mcm.dao;

import com.yuntongxun.mcm.core.exception.CCPRedisException;
import com.yuntongxun.mcm.mcm.model.MessageInfo;

public interface MessageRedisDao {

	/**
	 * 根据版本号获取存储redis中的消息key
	 * key:ytx042|userAcc|version
	 * value:{"dateCreated":"1450942745417","expired":"604800000","fileUrl":"","groupId":"","localFileName":"","mcmEvent":"0","msgCompressLen":"0","msgContent":"MjM0","msgDomain":"","msgFileName":"","msgFileSize":"","msgId":"DE423A8996C8B9B00C654678E7024CB5|6","msgLength":"24","msgReceiver":"54321","msgSender":"12345","msgType":"1","version":"2"}
	 * @param userAcc
	 * @param messageInfo
	 * @return
	 * @throws CCPRedisException
	 */
	void saveMessage(String userAcc, MessageInfo messageInfo) throws CCPRedisException;
	
}
