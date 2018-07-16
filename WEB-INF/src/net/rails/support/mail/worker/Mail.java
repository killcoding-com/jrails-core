package net.rails.support.mail.worker;

import java.io.File;
import java.security.KeyStore.PrivateKeyEntry;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mail {

	private final static Logger log = LoggerFactory.getLogger(Mail.class);
	public final static RecipientType TO = MimeMessage.RecipientType.TO;
	public final static RecipientType CC = MimeMessage.RecipientType.CC;
	public final static RecipientType BCC = MimeMessage.RecipientType.BCC;

	public static void send(RecipientType type, SendWorker sendWorker)
			throws Exception {
		final Properties pro = sendWorker.getProperties();
		Session session = null;
		if (sendWorker.isValiPwd()) {
			SMTPAuth authenticator = new SMTPAuth(sendWorker.getUserName(),sendWorker.getPassword());
			session = Session.getInstance(pro, authenticator);
		} else {
			session = Session.getInstance(pro);
		}
		session.setDebug(log.isDebugEnabled());
		final Message message = new MimeMessage(session);
		message.setFrom(sendWorker.getFromAddress());
		message.setRecipients(type, sendWorker.getInternetAddresses());
		message.setSubject(sendWorker.getSubject());
		message.setSentDate(sendWorker.getSentDate());

		Multipart mm = new MimeMultipart();
		BodyPart mbp = new MimeBodyPart();
		mbp.setContent(sendWorker.getContent(), sendWorker.getType());
		mm.addBodyPart(mbp);

		final List<File> files = sendWorker.getAttachFiles();
		for (Iterator<File> iterator = files.iterator(); iterator.hasNext();) {
			File file = iterator.next();
			MimeBodyPart filePart = new MimeBodyPart();
			FileDataSource fds = new FileDataSource(file);
			filePart.setDataHandler(new DataHandler(fds));
			filePart.setFileName(MimeUtility.encodeWord(fds.getName()));
			mm.addBodyPart(filePart);
		}

		message.setContent(mm);
		message.saveChanges();
		Transport.send(message);
	}

	public static Store getStore(Properties props, String storeType,
			String host, int port, String user, String pwd)
			throws Exception {
		final Session session = Session.getInstance(props);
		session.setDebug(log.isDebugEnabled());
		final Store store = session.getStore(storeType);
		store.connect(host, port, user, pwd);
		return store;
	}

}

class SMTPAuth extends Authenticator {

	private String userName;   
    private String password;   
        
    public SMTPAuth(){  
    	super();
    }   
    
    public SMTPAuth(String username, String password) {   
    	super();
        this.userName = username;    
        this.password = password;    
    }   
    
    protected PasswordAuthentication getPasswordAuthentication(){   
        return new PasswordAuthentication(userName, password);   
    }   

    
}
