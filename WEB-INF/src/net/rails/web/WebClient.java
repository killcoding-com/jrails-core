package net.rails.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebClient {

	protected boolean isHttps = false;
	protected HttpURLConnection conn;
	protected String method = "POST";
	protected String url;
	protected String qs;

	public WebClient(String url) {
		this.url = url;
	}
	
	public WebClient(String url, String qs) {
		this.url = url;
		this.qs = qs;
	}
	
	public int get() throws Exception{
		setMethod("GET");
		return connect();
	}
	
	public int post() throws Exception{
		setMethod("POST");
		return connect();
	}

	public int connect() throws Exception {
		URL connUrl = new URL(url);
		isHttps = connUrl.toString().indexOf("https://") == 0 ? true : false;
		if (isHttps) {
			SSLContext context = SSLContext.getInstance("SSL");
			context.init(null,
					new TrustManager[] { new TrustAnyTrustManager() },
					new SecureRandom());
			HttpsURLConnection httpsConn = null;
			if (method.equals("POST")) {
				conn = (HttpURLConnection) connUrl.openConnection();
				initConn();
				httpsConn = (HttpsURLConnection) conn;
				httpsConn.setRequestMethod(method);
				httpsConn.setSSLSocketFactory(context.getSocketFactory());
				httpsConn.setHostnameVerifier(new TrustAnyHostnameVerifier());
				httpsConn.setDoInput(true);
				httpsConn.setDoOutput(true);
				httpsConn.setUseCaches(false);
				if(qs != null){
					httpsConn.getOutputStream().write(qs.getBytes());
					httpsConn.getOutputStream().flush();
					httpsConn.getOutputStream().close();
				}
			} else if (method.equals("GET")) {
				connUrl = new URL(url
						+ (qs == null ? "" : "?" + qs));
				conn = (HttpURLConnection) connUrl.openConnection();
				initConn();
				httpsConn = (HttpsURLConnection) conn;
				httpsConn.setRequestMethod(method);
				httpsConn.setSSLSocketFactory(context.getSocketFactory());
				httpsConn.setHostnameVerifier(new TrustAnyHostnameVerifier());
				httpsConn.setDoInput(true);
				httpsConn.setDoOutput(true);
				httpsConn.setUseCaches(false);
			}
			conn.connect();
		} else {
			if (method.toUpperCase().equals("POST")) {
				conn = (HttpURLConnection) connUrl.openConnection();
				initConn();
				conn.setRequestMethod(method);
				conn.setUseCaches(false);
				conn.setDoInput(true);
				conn.setDoOutput(true);
				if(qs != null){
					conn.getOutputStream().write(qs.getBytes());
					conn.getOutputStream().flush();
					conn.getOutputStream().close();
				}
			} else if (method.toUpperCase().equals("GET")) {
				connUrl = new URL(url
						+ (qs == null ? "" : "?" + qs));
				conn = (HttpURLConnection) connUrl.openConnection();
				conn.setRequestMethod(method);
				conn.setUseCaches(false);
				conn.setDoInput(true);
				conn.setDoOutput(true);
			}			
			conn.connect();
		}
		return conn.getResponseCode();
	}
	
	public HttpURLConnection getConn(){
		return conn;
	}

	public void setMethod(String method) {
		this.method = method.toUpperCase();
	}
	
	public String getMethod(){
		return method;
	}
	
	public String getUrl(){
		return url;
	}

	public String getCharset() {
  //String charset = conn.getContentEncoding(); 
  //System.out.println("Charset: " + charset);
  //return charset == null ? "UTF-8" : charset;
		String ct = conn.getContentType();
  //System.out.println("ContentType: " + ct);
		if (ct != null) {
			Pattern pattern = Pattern
					.compile("(?<=(?i)(charset)=['\"]{0,1})([a-zA-Z0-9-/]+)(?=(['\"]{0,1}))");
			Matcher matcher = pattern.matcher(ct);
			return matcher.find() ? matcher.group().toUpperCase() : "UTF-8";
		} else {
		  	return null;
   }
	}

	public void setQs(String qs) {
		this.qs = qs;
	}

	public String getQs() {
		return qs;
	}

	public byte[] getResponseBytes() throws IOException {		
		InputStream input = null;
		ByteArrayOutputStream out = null;
		try {
			input = conn.getInputStream();
			out = new ByteArrayOutputStream(1024);
			byte[] buf = new byte[1024];
			int len = -1;
			while ((len = input.read(buf)) != -1) {
				out.write(buf, 0, len);
				out.flush();
			}
			return out.toByteArray();
		} finally {
			if (input != null)
				input.close();
			if (out != null)
				out.close();
		}
	}
	
	public byte[] getErrorBytes() throws IOException {		
		InputStream input = null;
		ByteArrayOutputStream out = null;
		try {
			input = conn.getErrorStream();
			out = new ByteArrayOutputStream(1024);
			byte[] buf = new byte[1024];
			int len = -1;
			while ((len = input.read(buf)) != -1) {
				out.write(buf, 0, len);
				out.flush();
			}
			return out.toByteArray();
		} finally {
			if (input != null)
				input.close();
			if (out != null)
				out.close();
		}
	}

	public String getResponseText() throws Exception {
		byte[] data = getResponseBytes();
		if (data != null)
			return new String(data, getCharset());
		else
			return null;
	}
	
	public String getErrorText() throws Exception {
		byte[] data = getErrorBytes();
		if (data != null)
			return new String(data, getCharset());
		else
			return null;
	}
	
	protected void initConn(){
		
	}

}

class TrustAnyHostnameVerifier implements HostnameVerifier {
	public boolean verify(String hostname, SSLSession session) {
		return true;
	}
}

class TrustAnyTrustManager implements X509TrustManager {
	public void checkClientTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
	}

	public void checkServerTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
	}

	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[] {};
	}
}
