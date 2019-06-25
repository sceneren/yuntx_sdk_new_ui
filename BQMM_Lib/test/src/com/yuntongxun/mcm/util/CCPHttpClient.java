package com.yuntongxun.mcm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ming.sample.util.ProtocolUtil;
import org.ming.sample.util.StreamUtil;

import com.yuntongxun.mcm.core.exception.CCPException;
import com.yuntongxun.mcm.fileserver.util.FileServerConstant;
import com.yuntongxun.mcm.weixin.util.WeiXinUtils;


/**
 * CCP Http Client based Apache HttpClient.
 * 
 * @since 2013-11-18
 * @version 1.0
 */
public final class CCPHttpClient {

	public static final Logger log = LogManager.getLogger(CCPHttpClient.class);
	public static final String TLS = "TLS";
	private int connTimeout;
	private int soTimeout;
	private PoolingClientConnectionManager cm = null; 
	private HttpPost httpPost;
	
	private HttpGet httpGet;
	
	private String fileTempDir="d:/temp_files";
	

	public void init() {
		cm = new PoolingClientConnectionManager();
        cm.setMaxTotal(32);
	}

	/**
	 * HTTP GET Request API
	 * 
	 * @param url
	 * @return
	 * @throws CCPException
	 */
	public Map<String,String> httpGet(String url) throws CCPException {
		return httpGet(url, null);
	}

	/**
	 *  * HTTP GET Request API.
	 *  
	 * @param url
	 * @param headers
	 * @return
	 * @throws CCPException
	 */
	public Map<String,String> httpGet(String url, HashMap<String, String> headers) throws CCPException {
		return httpGet(url, -1, headers);
	}

	/**
	 * HTTP GET Request API.
	 * 
	 * @param url
	 * @param headers
	 * @return
	 * @throws CCPException
	 */
	public Map<String,String> httpGet(String url, int port, HashMap<String, String> headers) throws CCPException {
		DefaultHttpClient httpClient = null;
		Map<String,String> httpResult = new Hashtable<String, String>();
		try {
			if (ProtocolUtil.isHttpsUrl(url)) {
				port = (port <= -1 ? 443 : 8883);
				httpClient = registerSSL(ProtocolUtil.getHost(url), TLS, port, "https");
			} else {
				httpClient = new DefaultHttpClient();
			}
			HttpGet httpRequest = new HttpGet(url);

			HttpConnectionParams.setConnectionTimeout(httpRequest.getParams(), connTimeout * 1000);
			HttpConnectionParams.setSoTimeout(httpRequest.getParams(), soTimeout * 1000);

			if (headers != null && headers.size() > 0) {
				Iterator<String> keys = headers.keySet().iterator();
				while (keys.hasNext()) {
					String key = keys.next();
					String value = headers.get(key);
					httpRequest.setHeader(key, value);
				}
			} else {
				httpRequest.setHeader("Content-Type", "application/xml");
			}

			log.debug(url);
			HttpResponse response = httpClient.execute(httpRequest);
			log.info("HTTP Get Send Over...");

			if (response != null) {
				int statusCode = response.getStatusLine().getStatusCode();
				log.info("Http Get status code: " + statusCode);
				String resp = null;
				if (statusCode == HttpStatus.SC_OK) {
					resp = StreamUtil.readContentByStream(response.getEntity().getContent());
					log.debug(resp);
				} else {
					throw new CCPException(ScriptManager.buildError("112605", "Got error code " + statusCode
							+ " from [" + url + "]"));
				}
				httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE, String.valueOf(statusCode));
				httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT, resp);
				return httpResult;
			}

			throw new CCPException(ScriptManager.buildError("112600", "Unable to connect to server. [" + url + "]"));
		} catch (KeyManagementException e) {
			throw new CCPException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new CCPException(e);
		} catch (IOException e) {
			throw new CCPException(e);
		} finally {
			/*if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}*/
		}
	}

	/**
	 * HTTP POST Request API
	 * 
	 * @param url
	 * @param requestBody
	 * @return
	 * @throws CCPException
	 */
	public Map<String, String> httpPost(String url, String requestBody) throws CCPException {
		return httpPost(url, null, requestBody);
	}

	/**
	 * HTTP POST Request API
	 * 
	 * @param url
	 * @param requestBody
	 * @return
	 * @throws CCPException
	 */
	public Map<String, String> httpPost(String url, HashMap<String, String> headers, String requestBody) throws CCPException {
		return httpPost(url, 8883, headers, requestBody);
	}

	/**
	 * HTTP POST Request API
	 * 
	 * @param url
	 * @param headers
	 * @param requestBody
	 * @return
	 * @throws CCPException
	 */
	public Map<String, String> httpPost(String url, int port, HashMap<String, String> headers, String requestBody)
			throws CCPException {
		DefaultHttpClient httpClient = null;
		Map<String, String> httpResult = new HashMap<String, String>();
		HttpResponse response = null;
		try {
			if (ProtocolUtil.isHttpsUrl(url)) {
				port = (port < 0 ? 443 : port);
				httpClient = registerSSL(ProtocolUtil.getHost(url), TLS, port, "https");
			} else {
				HttpParams paramsw = new BasicHttpParams();  
				HttpConnectionParams.setStaleCheckingEnabled(paramsw, false);  
				HttpConnectionParams.setConnectionTimeout(paramsw, connTimeout * 1000);  
				HttpConnectionParams.setSoTimeout(paramsw, soTimeout * 1000);  
				HttpConnectionParams.setSocketBufferSize(paramsw, 8192 * 5);  
				
				httpClient = new DefaultHttpClient(this.cm);
			}

			httpPost = new HttpPost(url);
			// 设置请求超时时间
			//HttpConnectionParams.setConnectionTimeout(httpPost.getParams(), connTimeout * 1000);
			// 设置等待数据超时时间
			//HttpConnectionParams.setSoTimeout(httpPost.getParams(), soTimeout * 1000);
			
			if (headers != null && headers.size() > 0) {
				Iterator<String> keys = headers.keySet().iterator();
				while (keys.hasNext()) {
					String key = keys.next();
					String value = headers.get(key);
					httpPost.setHeader(key, value);
					log.debug(key + ": " + value);
				}
			} else {
				httpPost.setHeader("Content-Type", "text/xml");
			}
			log.debug(url);
			log.debug(requestBody + "\r\n");

			byte[] requestByte = ProtocolUtil.getUTF8EncodingContent(requestBody);
			HttpEntity entity = new ByteArrayEntity(requestByte);
			httpPost.setEntity(entity);

			long httpStart = System.currentTimeMillis();
			log.info("Http Post Send Start...");
			response = httpClient.execute(httpPost);
			
			//判断响应header中 数据类型(Content-Type)，向返回map中标记数据类型
			Header[] responseHeaders = response.getHeaders("Content-Type");
			httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_DATAFORMAT, Constants.DATA_FORMAT_NONE);
			if(responseHeaders!=null&&responseHeaders.length>0){
				//只获取header数组中中第一组Content-Type
				Header responseHeader = responseHeaders[0];
				String headerValue = responseHeader.getValue().toLowerCase();
				if(StringUtils.isNotEmpty(headerValue)&&headerValue.indexOf(Constants.DATA_FORMAT_JSON)>0){
					httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_DATAFORMAT, Constants.DATA_FORMAT_JSON);
				}
				if(StringUtils.isNotEmpty(headerValue)&&headerValue.indexOf(Constants.DATA_FORMAT_XML)>0){
					httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_DATAFORMAT, Constants.DATA_FORMAT_XML);
				}
			}
			
			long httpEnd = System.currentTimeMillis();
			log.info("Http Post Send Over...cost: "+(httpEnd-httpStart)+"ms");

			if (response != null) {
				int statusCode = response.getStatusLine().getStatusCode();
				String resp = null;
				log.info("Http Post status code: " + statusCode + "\r\n");
				if (statusCode == HttpStatus.SC_OK) {
					resp = StreamUtil.readContentByStream(response.getEntity().getContent());
					log.debug(resp);
				} else {
					httpPost.abort();
					throw new CCPException(ScriptManager.buildError("112605", "Got error code " + statusCode
							+ " from [" + url + "]"));
				}
				httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE, String.valueOf(statusCode));
				httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT, resp);
				log.info("@http request:url: " + url + ",statusCode:"
						+ statusCode + ",content:" + resp + "\r\n");
				return httpResult;
			}

		} catch (NoSuchAlgorithmException nsaio) {
			 if(httpPost.isAborted()){
			     httpPost.abort();
			    }
			throw new CCPException(nsaio);
		} catch (KeyManagementException ke) {
			 if(httpPost.isAborted()){
			     httpPost.abort();
			    }
			throw new CCPException(ke);
		} catch (IOException ioe) {
			 if(httpPost.isAborted()){
			     httpPost.abort();
			    }
			throw new CCPException(ioe);
		} finally {
			/*if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}*/
		}
		
		return null;
	}

	/**
	 * 注册SSL连接
	 * 
	 * @param hostname
	 *            请求的主机名（IP或者域名）
	 * @param protocol
	 *            请求协议名称（TLS-安全传输层协议）
	 * @param port
	 *            端口号
	 * @param scheme
	 *            协议名称
	 * @return HttpClient实例
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	DefaultHttpClient registerSSL(String hostname, String protocol, int port, String scheme)
			throws NoSuchAlgorithmException, KeyManagementException {

		// 创建一个默认的HttpClient
		DefaultHttpClient httpclient = new DefaultHttpClient();
		// 创建SSL上下文实例
		SSLContext ctx = SSLContext.getInstance(protocol);
		// 服务端证书验证
		X509TrustManager tm = new X509TrustManager() {

			/**
			 * 验证客户端证书
			 */
			public void checkClientTrusted(X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {
				// 这里跳过客户端证书验证
			}

			/**
			 * 验证服务端证书
			 * 
			 * @param chain
			 *            证书链
			 * @param authType
			 *            使用的密钥交换算法，当使用来自服务器的密钥时authType为RSA
			 */
			public void checkServerTrusted(X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {
				if (chain == null || chain.length == 0)
					throw new IllegalArgumentException("null or zero-length certificate chain");
				if (authType == null || authType.length() == 0)
					throw new IllegalArgumentException("null or zero-length authentication type");
				log.info("authType: " + authType);
				boolean br = false;
				Principal principal = null;
				for (X509Certificate x509Certificate : chain) {
					principal = x509Certificate.getSubjectX500Principal();
					if (principal != null) {
						log.info("服务器证书信息: " + principal.getName());
						br = true;
						return;
					}
				}
				if (!br) {
					throw new CertificateException("服务端证书验证失败！");
				}
			}

			/**
			 * 返回CA发行的证书
			 */
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		};

		// 初始化SSL上下文
		ctx.init(null, new TrustManager[] { tm }, new java.security.SecureRandom());
		// 创建SSL连接
		SSLSocketFactory socketFactory = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		Scheme sch = new Scheme(scheme, port, socketFactory);
		// 注册SSL连接
		httpclient.getConnectionManager().getSchemeRegistry().register(sch);
		return httpclient;
	}
	
	
	/**
	 * HTTP POST Request API
	 * 
	 * @param url
	 * @param headers
	 * @param requestBody
	 * @return
	 * @throws CCPException
	 */
	public Map<String, String> httpPostForMultiEntity(String url, int port, Map<String, String> headers, String filePath)
			throws CCPException {
//		filePath = "d:/temp_files/test.jpg";
		DefaultHttpClient httpClient = null;
		Map<String, String> httpResult = new HashMap<String, String>();
		try {
			if (ProtocolUtil.isHttpsUrl(url)) {
				port = (port < 0 ? 443 : port);
				httpClient = registerSSL(ProtocolUtil.getHost(url), TLS, port, "https");
			} else {
				httpClient = new DefaultHttpClient(this.cm);
			}
			
//			BasicHttpEntity entity = new BasicHttpEntity();
			File uploadFile = new File(filePath);
//			InputStream in = new FileInputStream(uploadFile);
//			entity.setContent(in);
//			entity.setContentLength(in.available());
			MultipartEntity mutiEntity = new MultipartEntity();
			mutiEntity.addPart("media",new FileBody(uploadFile));
			httpPost = new HttpPost(url);
//			httpPost.setHeader("Content-Type", "application/octet-stream");
			// 设置请求超时时间
			HttpConnectionParams.setConnectionTimeout(httpPost.getParams(), connTimeout * 1000);
			// 设置等待数据超时时间
			HttpConnectionParams.setSoTimeout(httpPost.getParams(), soTimeout * 1000);
			
			if (headers != null && headers.size() > 0) {
				Iterator<String> keys = headers.keySet().iterator();
				while (keys.hasNext()) {
					String key = keys.next();
					String value = headers.get(key);
					httpPost.setHeader(key, value);
					log.debug(key + ": " + value);
				}
			}
			log.debug(url);
			log.debug("upload file path:"+filePath+",file length:"+ uploadFile.length() + "\r\n");

			httpPost.setEntity(mutiEntity);

			long httpStart = System.currentTimeMillis();
			log.info("Http Post Send Start...");
			HttpResponse response = httpClient.execute(httpPost);
			
			//判断响应header中 数据类型(Content-Type)，向返回map中标记数据类型
			Header[] responseHeaders = response.getHeaders("Content-Type");
			httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_DATAFORMAT, Constants.DATA_FORMAT_NONE);
			if(responseHeaders!=null&&responseHeaders.length>0){
				//只获取header数组中中第一组Content-Type
				Header responseHeader = responseHeaders[0];
				String headerValue = responseHeader.getValue().toLowerCase();
				if(StringUtils.isNotEmpty(headerValue)&&headerValue.indexOf(Constants.DATA_FORMAT_JSON)>0){
					httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_DATAFORMAT, Constants.DATA_FORMAT_JSON);
				}
				if(StringUtils.isNotEmpty(headerValue)&&headerValue.indexOf(Constants.DATA_FORMAT_XML)>0){
					httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_DATAFORMAT, Constants.DATA_FORMAT_XML);
				}
			}
			
			long httpEnd = System.currentTimeMillis();
			log.info("Http Post Send Over...cost: "+(httpEnd-httpStart)+"ms");

			if (response != null) {
				int statusCode = response.getStatusLine().getStatusCode();
				String resp = null;
				log.info("Http Post status code: " + statusCode + "\r\n");
				if (statusCode == HttpStatus.SC_OK) {
					resp = StreamUtil.readContentByStream(response.getEntity().getContent());
					log.debug(resp);
				} else {
					throw new CCPException(ScriptManager.buildError("112605", "Got error code " + statusCode
							+ " from [" + url + "]"));
				}
				httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE, String.valueOf(statusCode));
				httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT, resp);
				log.info("@http request:url: " + url + ",statusCode:"
						+ statusCode + ",content:" + resp + "\r\n");
				return httpResult;
			}
			throw new CCPException(ScriptManager.buildError("112600", "Unable to connect to server. [" + url + "]"));

		} catch (NoSuchAlgorithmException nsaio) {
			 if(httpPost.isAborted()){
			     httpPost.abort();
			    }
			throw new CCPException(nsaio);
		} catch (KeyManagementException ke) {
			 if(httpPost.isAborted()){
			     httpPost.abort();
			    }
			throw new CCPException(ke);
		} catch (IOException ioe) {
			 if(httpPost.isAborted()){
			     httpPost.abort();
			    }
			throw new CCPException(ioe);
		} finally {
			/*if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}*/
		}
	}
	
	/**
	 * HTTP POST Request API
	 * 
	 * @param url
	 * @param headers
	 * @param requestBody
	 * @return
	 * @throws CCPException
	 */
	public Map<String, String> uploadFileToFileServer(String url, int port, Map<String, String> headers, String filePath)
			throws CCPException {
		DefaultHttpClient httpClient = null;
		Map<String, String> httpResult = new HashMap<String, String>();
		try {
			if (ProtocolUtil.isHttpsUrl(url)) {
				port = (port < 0 ? 443 : port);
				httpClient = registerSSL(ProtocolUtil.getHost(url), TLS, port, "https");
			} else {
				httpClient = new DefaultHttpClient(this.cm);
			}
			
			BasicHttpEntity entity = new BasicHttpEntity();
			File uploadFile = new File(filePath);
			InputStream in = new FileInputStream(uploadFile);
			entity.setContent(in);
			entity.setContentLength(in.available());
//			MultipartEntity mutiEntity = new MultipartEntity();
//			mutiEntity.addPart("media",new FileBody(uploadFile));
			httpPost = new HttpPost(url);
			httpPost.setHeader("Content-Type", "application/octet-stream");
			// 设置请求超时时间
			HttpConnectionParams.setConnectionTimeout(httpPost.getParams(), connTimeout * 1000);
			// 设置等待数据超时时间
			HttpConnectionParams.setSoTimeout(httpPost.getParams(), soTimeout * 1000);
			
			if (headers != null && headers.size() > 0) {
				Iterator<String> keys = headers.keySet().iterator();
				while (keys.hasNext()) {
					String key = keys.next();
					String value = headers.get(key);
					httpPost.setHeader(key, value);
					log.debug(key + ": " + value);
				}
			}
			log.debug(url);
			log.debug("upload file path:"+filePath+",file length:"+ uploadFile.length() + "\r\n");

			httpPost.setEntity(entity);

			long httpStart = System.currentTimeMillis();
			log.info("Http Post Send Start...");
			HttpResponse response = httpClient.execute(httpPost);
			
			//判断响应header中 数据类型(Content-Type)，向返回map中标记数据类型
			Header[] responseHeaders = response.getHeaders("Content-Type");
			httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_DATAFORMAT, Constants.DATA_FORMAT_NONE);
			if(responseHeaders!=null&&responseHeaders.length>0){
				//只获取header数组中中第一组Content-Type
				Header responseHeader = responseHeaders[0];
				String headerValue = responseHeader.getValue().toLowerCase();
				if(StringUtils.isNotEmpty(headerValue)&&headerValue.indexOf(Constants.DATA_FORMAT_JSON)>0){
					httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_DATAFORMAT, Constants.DATA_FORMAT_JSON);
				}
				if(StringUtils.isNotEmpty(headerValue)&&headerValue.indexOf(Constants.DATA_FORMAT_XML)>0){
					httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_DATAFORMAT, Constants.DATA_FORMAT_XML);
				}
			}
			
			long httpEnd = System.currentTimeMillis();
			log.info("Http Post Send Over...cost: "+(httpEnd-httpStart)+"ms");

			if (response != null) {
				int statusCode = response.getStatusLine().getStatusCode();
				String resp = null;
				log.info("Http Post status code: " + statusCode + "\r\n");
				if (statusCode == HttpStatus.SC_OK) {
					resp = StreamUtil.readContentByStream(response.getEntity().getContent());
					log.debug(resp);
				} else {
					throw new CCPException(ScriptManager.buildError("112605", "Got error code " + statusCode
							+ " from [" + url + "]"));
				}
				httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE, String.valueOf(statusCode));
				httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT, resp);
				log.info("@http request:url: " + url + ",statusCode:"
						+ statusCode + ",content:" + resp + "\r\n");
				return httpResult;
			}
			throw new CCPException(ScriptManager.buildError("112600", "Unable to connect to server. [" + url + "]"));

		} catch (NoSuchAlgorithmException nsaio) {
			 if(httpPost.isAborted()){
			     httpPost.abort();
			    }
			throw new CCPException(nsaio);
		} catch (KeyManagementException ke) {
			 if(httpPost.isAborted()){
			     httpPost.abort();
			    }
			throw new CCPException(ke);
		} catch (IOException ioe) {
			 if(httpPost.isAborted()){
			     httpPost.abort();
			    }
			throw new CCPException(ioe);
		} finally {
			/*if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}*/
		}
	}
	
	/**
	 * 下载文件，保存到本地磁盘（post请求）
	 * @param url
	 * @param port
	 * @return filePath
	 * @throws CCPException
	 */
	public Map<String,String> downloadFilePost(String url, int port)
			throws CCPException {		
		String extendFileName = WeiXinUtils.getExtentName(url);
		Map<String,String> httpResult = new Hashtable<String, String>();
		String result = null;
		DefaultHttpClient httpClient = null;
		try {
			if (ProtocolUtil.isHttpsUrl(url)) {
				port = (port < 0 ? 443 : port);
				httpClient = registerSSL(ProtocolUtil.getHost(url), TLS, port, "https");
			} else {
				httpClient = new DefaultHttpClient(this.cm);
			}

			httpPost = new HttpPost(url);
			// 设置请求超时时间
			HttpConnectionParams.setConnectionTimeout(httpPost.getParams(), connTimeout * 1000);
			// 设置等待数据超时时间
			HttpConnectionParams.setSoTimeout(httpPost.getParams(), soTimeout * 1000);
			
			log.debug(url);

			long httpStart = System.currentTimeMillis();
			log.info("Http Post Send Start...");
			HttpResponse response = httpClient.execute(httpPost);
			
			long httpEnd = System.currentTimeMillis();
			log.info("Http Post Send Over...cost: "+(httpEnd-httpStart)+"ms");

			if (response != null) {
				int statusCode = response.getStatusLine().getStatusCode();
				String resp = null;
				log.info("Http Post status code: " + statusCode + "\r\n");
				if (statusCode == HttpStatus.SC_OK) {
					File fileTempPath = new File(fileTempDir);
					if(!fileTempPath.exists()){
						fileTempPath.mkdirs();
					}
					//随机生成文件名
					String newFileName = StringUtil.getUUID()+extendFileName;
					String filePath = fileTempDir+File.separator+newFileName;
					//保存数据到本地
					File file = new File(filePath);
					FileOutputStream out = null;
					InputStream in = null;
					try{
						out = new FileOutputStream(file);
						in = response.getEntity().getContent();
						byte[] buffer = new byte[1024*128];
						int c;
						while((c=in.read(buffer,0,buffer.length))!=-1 ){
							out.write(buffer,0,c);
							out.flush();
						}
					}finally{
						if(out!=null){
							out.close();
						}
						if(in!=null){
							in.close();
						}
					}
					result = filePath;
				} else {
					throw new CCPException(ScriptManager.buildError("112605", "Got error code " + statusCode
							+ " from [" + url + "]"));
				}
				httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE, String.valueOf(statusCode));
				httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT, result);
				log.info("@http request:url: " + url + ",statusCode:"
						+ statusCode + ",content:" + result + "\r\n");
				return httpResult;
			}
			throw new CCPException(ScriptManager.buildError("112600", "Unable to connect to server. [" + url + "]"));

		} catch (NoSuchAlgorithmException nsaio) {
			 if(httpPost.isAborted()){
			     httpPost.abort();
			    }
			throw new CCPException(nsaio);
		} catch (KeyManagementException ke) {
			 if(httpPost.isAborted()){
			     httpPost.abort();
			    }
			throw new CCPException(ke);
		} catch (IOException ioe) {
			 if(httpPost.isAborted()){
			     httpPost.abort();
			    }
			throw new CCPException(ioe);
		} finally {
			/*if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}*/
		}
	}
	
	/**
	 * 下载文件，保存到本地磁盘(get请求)
	 * @param url
	 * @param port
	 * @return filePath
	 * @throws CCPException
	 */
	public Map<String,String> downloadFileGet(String url,int port)
			throws CCPException {
		String extendFileName = WeiXinUtils.getExtentName(url);
		Map<String,String> httpResult = new Hashtable<String, String>();
		String result = null;
		DefaultHttpClient httpClient = null;
		try {
			if (ProtocolUtil.isHttpsUrl(url)) {
				port = (port < 0 ? 443 : port);
				httpClient = registerSSL(ProtocolUtil.getHost(url), TLS, port, "https");
			} else {
				httpClient = new DefaultHttpClient(this.cm);
			}

			httpGet = new HttpGet(url);
			// 设置请求超时时间
			HttpConnectionParams.setConnectionTimeout(httpGet.getParams(), connTimeout * 1000);
			// 设置等待数据超时时间
			HttpConnectionParams.setSoTimeout(httpGet.getParams(), soTimeout * 1000);
			
			log.debug(url);

			long httpStart = System.currentTimeMillis();
			log.info("Http Get Send Start...");
			HttpResponse response = httpClient.execute(httpGet);
			
			long httpEnd = System.currentTimeMillis();
			log.info("Http Get Send Over...cost: "+(httpEnd-httpStart)+"ms");

			if (response != null) {
				int statusCode = response.getStatusLine().getStatusCode();
				String resp = null;
				log.info("Http Post status code: " + statusCode + "\r\n");
				if (statusCode == HttpStatus.SC_OK) {
					File fileTempPath = new File(fileTempDir);
					if(!fileTempPath.exists()){
						fileTempPath.mkdirs();
					}
					//随机生成文件名
					String newFileName = StringUtil.getUUID()+extendFileName;
					String filePath = fileTempDir+File.separator+newFileName;
					//保存数据到本地
					File file = new File(filePath);
					FileOutputStream out = null;
					InputStream in = null;
					try{
						out = new FileOutputStream(file);
						in = response.getEntity().getContent();
						byte[] buffer = new byte[1024*128];
						int c;
						while((c=in.read(buffer,0,buffer.length))!=-1 ){
							out.write(buffer,0,c);
							out.flush();
						}
					}finally{
						if(out!=null){
							out.close();
						}
						if(in!=null){
							in.close();
						}
					}
					result = filePath;
				} else {
					throw new CCPException(ScriptManager.buildError("112605", "Got error code " + statusCode
							+ " from [" + url + "]"));
				}
				log.info("@http request:url: " + url + ",statusCode:"
						+ statusCode + ",content:" + result + "\r\n");
				httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE, String.valueOf(statusCode));
				httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT, String.valueOf(result));
				return httpResult;
			}
			throw new CCPException(ScriptManager.buildError("112600", "Unable to connect to server. [" + url + "]"));

		} catch (NoSuchAlgorithmException nsaio) {
			 if(httpGet.isAborted()){
				 httpGet.abort();
			    }
			throw new CCPException(nsaio);
		} catch (KeyManagementException ke) {
			 if(httpGet.isAborted()){
				 httpGet.abort();
			    }
			throw new CCPException(ke);
		} catch (IOException ioe) {
			 if(httpGet.isAborted()){
				 httpGet.abort();
			    }
			throw new CCPException(ioe);
		} finally {
			/*if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}*/
		}
	}
	
	public Map<String, String> uploadFileToFileServer(String url, int port, Map<String, String> headers, InputStream in)
			throws CCPException {
		DefaultHttpClient httpClient = null;
		Map<String, String> httpResult = new HashMap<String, String>();
		try {
			if (ProtocolUtil.isHttpsUrl(url)) {
				port = (port < 0 ? 443 : port);
				httpClient = registerSSL(ProtocolUtil.getHost(url), TLS, port, "https");
			} else {
				httpClient = new DefaultHttpClient(this.cm);
			}
			
			BasicHttpEntity entity = new BasicHttpEntity();
			entity.setContent(in);
			entity.setContentLength(in.available());
			httpPost = new HttpPost(url);
			httpPost.setHeader("Content-Type", "application/octet-stream");
			// 设置请求超时时间
			HttpConnectionParams.setConnectionTimeout(httpPost.getParams(), connTimeout * 1000);
			// 设置等待数据超时时间
			HttpConnectionParams.setSoTimeout(httpPost.getParams(), soTimeout * 1000);
			
			if (headers != null && headers.size() > 0) {
				Iterator<String> keys = headers.keySet().iterator();
				while (keys.hasNext()) {
					String key = keys.next();
					String value = headers.get(key);
					httpPost.setHeader(key, value);
					log.debug(key + ": " + value);
				}
			}
			log.debug(url);

			httpPost.setEntity(entity);

			long httpStart = System.currentTimeMillis();
			log.info("Http Post Send Start...");
			HttpResponse response = httpClient.execute(httpPost);
			
			//判断响应header中 数据类型(Content-Type)，向返回map中标记数据类型
			Header[] responseHeaders = response.getHeaders("Content-Type");
			httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_DATAFORMAT, Constants.DATA_FORMAT_NONE);
			if(responseHeaders!=null&&responseHeaders.length>0){
				//只获取header数组中中第一组Content-Type
				Header responseHeader = responseHeaders[0];
				String headerValue = responseHeader.getValue().toLowerCase();
				if(StringUtils.isNotEmpty(headerValue)&&headerValue.indexOf(Constants.DATA_FORMAT_JSON)>0){
					httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_DATAFORMAT, Constants.DATA_FORMAT_JSON);
				}
				if(StringUtils.isNotEmpty(headerValue)&&headerValue.indexOf(Constants.DATA_FORMAT_XML)>0){
					httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_DATAFORMAT, Constants.DATA_FORMAT_XML);
				}
			}
			
			long httpEnd = System.currentTimeMillis();
			log.info("Http Post Send Over...cost: "+(httpEnd-httpStart)+"ms");

			if (response != null) {
				int statusCode = response.getStatusLine().getStatusCode();
				String resp = null;
				log.info("Http Post status code: " + statusCode + "\r\n");
				if (statusCode == HttpStatus.SC_OK) {
					resp = StreamUtil.readContentByStream(response.getEntity().getContent());
					log.debug(resp);
				} else {
					throw new CCPException(ScriptManager.buildError("112605", "Got error code " + statusCode
							+ " from [" + url + "]"));
				}
				httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE, String.valueOf(statusCode));
				httpResult.put(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT, resp);
				log.info("@http request:url: " + url + ",statusCode:"
						+ statusCode + ",content:" + resp + "\r\n");
				return httpResult;
			}
			throw new CCPException(ScriptManager.buildError("112600", "Unable to connect to server. [" + url + "]"));

		} catch (NoSuchAlgorithmException nsaio) {
			 if(httpPost.isAborted()){
			     httpPost.abort();
			    }
			throw new CCPException(nsaio);
		} catch (KeyManagementException ke) {
			 if(httpPost.isAborted()){
			     httpPost.abort();
			    }
			throw new CCPException(ke);
		} catch (IOException ioe) {
			 if(httpPost.isAborted()){
			     httpPost.abort();
			    }
			throw new CCPException(ioe);
		} finally {
			/*if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}*/
		}
	}
	
	public int getConnTimeout() {
		return connTimeout;
	}

	public void setConnTimeout(int connTimeout) {
		this.connTimeout = connTimeout;
	}

	public int getSoTimeout() {
		return soTimeout;
	}

	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}
	
	public String getFileTempDir() {
		return fileTempDir;
	}

	public void setFileTempDir(String fileTempDir) {
		this.fileTempDir = fileTempDir;
	}

	public static void main(String[] agrs) throws CCPException{
		
		CCPHttpClient httpClient = new CCPHttpClient();
		String url = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token=SdzBKRm0t-lm2QDdnlVAgy6N-chmEQEig-zurQCLqUzmjqQFCu9aYkNC4xdlvDyc7dT5AzrxiLGW-MsKhNPO_wycGxiKszQwExXj_eARTsI&type=image";
		int port = 443;
		String filePath = "D:/weixin_temp_files/logo.jpg";
		Map<String,String> headers = new Hashtable<String, String>();
////		headers.put("", value);
		httpClient.httpPostForMultiEntity(url, port, headers, filePath);
		
		StringBuilder urlStringBuilder = new StringBuilder();
		String sig = "2B9C64616C98A93F1375BF0A2F6429E7";
		urlStringBuilder.append("http://123.56.149.246:8090")
						.append(FileServerConstant.URI_VTM_FILE_UPLOAD_INTERFACE)
						.append("?")
						.append("appId=").append("test_appId")
						.append("&")
						.append("userName=").append("test_userName")
						.append("&")
						.append("fileName=").append("test_fileName.txt")
						.append("&")
						.append("sig=").append(sig);
		String finalUrl = urlStringBuilder.toString();
		httpClient.httpPostForMultiEntity(finalUrl, -1, headers, filePath);
//		String downloadFile = "http://123.56.149.246:8888/vtm/test_appId/test_userName/1438845387286896987.txt";
//		httpClient.downloadFilePost(downfielPath, -1);
	}
	
	public void uploadddd(){
		
	}
}
