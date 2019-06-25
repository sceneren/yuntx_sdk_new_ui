package com.yuntongxun.mcm.core;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.MessageLite;
import com.yuntongxun.common.protobuf.ProtobufCodecManager;
import com.yuntongxun.mcm.core.protobuf.MsgLite.MsgLiteInner;

public abstract class AbstractController {

	public final Logger logger = LogManager.getLogger(getName());

	private static Map<Integer, AbstractController> controllerMap = new ConcurrentHashMap<Integer, AbstractController>();

	public AbstractController() {
	}

	protected abstract String getName();

	/**
	 * 
	 * @param protoType
	 * @return
	 */
	public static AbstractController getController(int protoType) {
		return controllerMap.get(protoType);
	}

	/**
	 * register controller
	 * 
	 * @param controller
	 * @param array
	 */
	public void register(AbstractController controller, int[] array) {
		if (controller != null && array != null) {
			for (int type : array) {
				controllerMap.put(type, controller);
			}
		} else {
			logger.info("Controller and Type can't null.");
		}
	}

	/**
	 * Post request by MQ
	 * 
	 * @param msgLite
	 * @param channelId
	 * @throws Exception
	 */
	public void postRequest(String channelId, MsgLiteInner msgLite) throws Exception {
	}

	/**
	 * @param message
	 * @param buffer
	 * @throws Exception
	 */
	protected MessageLite decoder(MessageLite lite, byte[] byteArray) throws Exception {
		return ProtobufCodecManager.decoder(lite, byteArray);
	}

	/**
	 * @param messageLite
	 * @return
	 * @throws IOException
	 */
	protected byte[] encoder(MessageLite messageLite) throws IOException {
		return ProtobufCodecManager.encoder(messageLite);
	}

}
