package com.yuntongxun.mcm.http;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ming.sample.util.ProtocolUtil;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.yuntongxun.mcm.core.exception.CCPException;
import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.util.CCPHttpClient;
import com.yuntongxun.mcm.util.ScriptManager;

/**
 * think about thread security, can't defined shared variable.
 * 
 * @author chao
 */
@Service
public class HttpClientImpl implements HttpClient {

	public static final Logger log = LogManager.getLogger(HttpClientImpl.class);

	private CCPHttpClient ccpHttpClient;
	
	@Override
	public Map<String, String> sendPacket(final String url, HttpMethod method, HashMap<String, String> header, 
			final String body) throws CCPServiceException {
		Map<String, String> httpResult = new HashMap<String, String>();
		if (!ProtocolUtil.checkUrl(url)) {
			throw new CCPServiceException(ScriptManager.buildError("112601"));
		}

		log.info(ProtocolUtil.getDispatcherPacket(header, body));

		try {
			if (method == HttpMethod.GET) {
				httpResult = ccpHttpClient.httpGet(url, header);
				return httpResult;

			} else if (method == HttpMethod.POST) {
				if (body == null) {
					throw new CCPServiceException(ScriptManager.buildError("112604"));
				}

				int port = ProtocolUtil.getPort(url);
				httpResult = ccpHttpClient.httpPost(url, port, header, body);
				return httpResult;

			} else {
				throw new CCPServiceException(ScriptManager.buildError("112602", "No support method [" + method.name()
						+ "]"));
			}
			
		} catch (CCPException e) {
			log.error("sendPacket#error()", e);
			if(e.getCause() instanceof java.net.SocketTimeoutException){
				return httpResult;
			}
		}
		
		return httpResult;
	}
	
	@Override
	public Map<String, String> uploadFile(final String url, HttpMethod method, HashMap<String, String> header, 
			final String filePath) throws CCPServiceException {
		Map<String, String> httpResult = new HashMap<String, String>();
		if (!ProtocolUtil.checkUrl(url)) {
			throw new CCPServiceException(ScriptManager.buildError("112601"));
		}

		log.info("" + ProtocolUtil.getDispatcherPacket(header, filePath));

		try {
			if (method == HttpMethod.GET) {
				return null;

			} else if (method == HttpMethod.POST) {
				if (filePath == null) {
					throw new CCPServiceException(ScriptManager.buildError("112604"));
				}

				int port = ProtocolUtil.getPort(url);
				httpResult = ccpHttpClient.httpPostForMultiEntity(url, port, header, filePath);
				return httpResult;

			} else {
				throw new CCPServiceException(ScriptManager.buildError("112602", "No support method [" + method.name()
						+ "]"));
			}
		} catch (CCPException e) {
			log.error("uploadFile#error()", e);
			if( e.getCause() instanceof java.net.SocketTimeoutException) {
				return httpResult;
			}
			
		}
		
		return httpResult;
	}
	
	@Override
	public Map<String, String> uploadFileToFileServer(final String url, HttpMethod method, HashMap<String, String> header, 
			final String filePath) throws CCPServiceException {
		Map<String, String> httpResult = new HashMap<String, String>();
		if (!ProtocolUtil.checkUrl(url)) {
			throw new CCPServiceException(ScriptManager.buildError("112601"));
		}

		log.info("" + ProtocolUtil.getDispatcherPacket(header, filePath));

		try {
			if (method == HttpMethod.GET) {
				return null;

			} else if (method == HttpMethod.POST) {
				if (filePath == null) {
					throw new CCPServiceException(ScriptManager.buildError("112604"));
				}

				int port = ProtocolUtil.getPort(url);
				httpResult = ccpHttpClient.uploadFileToFileServer(url, port, header, filePath);
				return httpResult;

			} else {
				throw new CCPServiceException(ScriptManager.buildError("112602", "No support method [" + method.name()
						+ "]"));
			}
		} catch (CCPException e) {
			log.error("uploadFileToFileServer#error()", e);
			if( e.getCause() instanceof java.net.SocketTimeoutException) {
				return httpResult;
			}
			
		}
		
		return httpResult;
	}
	
	@Override
	public Map<String,String> downloadFile(final String url, HttpMethod method)
			throws CCPServiceException {
		Map<String,String> httpResult = null;
		if (!ProtocolUtil.checkUrl(url)) {
			throw new CCPServiceException(ScriptManager.buildError("112601"));
		}

		try {
			if (method == HttpMethod.GET) {
				int port = ProtocolUtil.getPort(url);
				httpResult = ccpHttpClient.downloadFileGet(url, port);
				return httpResult;

			} else if (method == HttpMethod.POST) {

				int port = ProtocolUtil.getPort(url);
				httpResult = ccpHttpClient.downloadFilePost(url, port);
				return httpResult;

			} else {
				throw new CCPServiceException(ScriptManager.buildError("112602", "No support method [" + method.name()
						+ "]"));
			}
		} catch (CCPException e) {
			log.error("downloadFile#error()", e);
			if( e.getCause() instanceof java.net.SocketTimeoutException){
				return httpResult;
			}
			
		}
		
		return httpResult;
	}

	@Override
	public Map<String, String> uploadFileToFileServer(String url,
			HttpMethod method, HashMap<String, String> header, InputStream is)
			throws CCPServiceException {
		Map<String, String> httpResult = new HashMap<String, String>();
		if (!ProtocolUtil.checkUrl(url)) {
			throw new CCPServiceException(ScriptManager.buildError("112601"));
		}

		try {
			if (method == HttpMethod.GET) {

				return null;

			} else if (method == HttpMethod.POST) {
				if (is == null) {
					throw new CCPServiceException(ScriptManager.buildError("112604"));
				}

				int port = ProtocolUtil.getPort(url);
				httpResult = ccpHttpClient.uploadFileToFileServer(url, port, header, is);
				return httpResult;

			} else {
				throw new CCPServiceException(ScriptManager.buildError("112602", "No support method [" + method.name()
						+ "]"));
			}
		} catch (CCPException e) {
			log.error("uploadFileToFileServer#error()", e);
			if( e.getCause() instanceof java.net.SocketTimeoutException){
				return httpResult;
			}
			
		}
		
		return httpResult;
	}
	
	public CCPHttpClient getCcpHttpClient() {
		return ccpHttpClient;
	}

	public void setCcpHttpClient(CCPHttpClient ccpHttpClient) {
		this.ccpHttpClient = ccpHttpClient;
	}
}
