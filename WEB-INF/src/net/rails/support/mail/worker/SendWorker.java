package net.rails.support.mail.worker;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.internet.InternetAddress;

public final class SendWorker {
	
	private String userName;
	private String password;
	private String subject;
	private String content;
	private String type = "text/html; charset=utf-8";
	
	private boolean valiPwd = false;
	
	private Properties properties; 
	
	private Date sentDate = new Date();

	private InternetAddress fromAddress;
	private List<File> attachFiles = new ArrayList<File>();
	private List<InternetAddress> addresses = new ArrayList<InternetAddress>();
	
	public SendWorker(){
		super();
	}
	
	public Properties getProperties() {
		return properties;
	}
	
	public void setProperties(Properties properties){
		this.properties = properties;
		if(isValiPwd())
			this.properties.put("mail.smtp.auth","true");
	}

	public void addAttachFile(File file){
		attachFiles.add(file);
	}
	
	public void removeAttachFile(File file){
		attachFiles.remove(file);
	}
	
	public void removeAttachFile(int index){
		attachFiles.remove(index);
	}
	
	public void clearAttachFile(){
		attachFiles.clear();
	}
	
	public void containsAttachFile(File file){
		attachFiles.contains(file);
	}

	public List<File> getAttachFiles() {
		return attachFiles;
	}

	public void setAttachFiles(List<File> files) {
		this.attachFiles = files;
	}

	public InternetAddress getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(InternetAddress fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String textContent) {
		this.content = textContent;
	}
	
	public void addAddress(InternetAddress address){
		this.addresses.add(address);
	}

	public void removeAddress(InternetAddress address){
		this.addresses.remove(address);
	}
	
	public void removeAddress(int index){
		this.addresses.remove(index);
	}
	
	public void containsAddress(InternetAddress address){
		this.addresses.contains(address);
	}
	
	public void clearAddress(){
		this.addresses.clear();
	}
	
	public void setAddresses(List<InternetAddress> addresses) {
		this.addresses = addresses;
	}
	
	public List<InternetAddress> getAddresses() {
		return addresses;
	}
	
	public InternetAddress[] getInternetAddresses() {
		return addresses.toArray(new InternetAddress[addresses.size()]);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public Date getSentDate() {
		return sentDate;
	}

	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}

	public boolean isValiPwd() {
		return valiPwd;
	}

	public void setValiPwd(boolean valiPwd) {
		this.valiPwd = valiPwd;
	}	
	
}
