package com.isentia.util;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailWithAttachment {
	   
	  public static void sendEmailWithAttachement(String to,final String from,String bccList,final String password, String host,String[] attachment_PathList,String subject,String content) {
	   
      Properties props = new Properties();
      props.put("mail.smtp.auth", "false");
      props.put("mail.smtp.starttls.enable", "true");
      props.put("mail.smtp.host", host);
      props.put("mail.smtp.port", "25");

      // Get the Session object.
//      Session session = Session.getInstance(props,
//         new javax.mail.Authenticator() {
//            protected PasswordAuthentication getPasswordAuthentication() {
//               return new PasswordAuthentication(from, password);
//            }
//         });
      Session session = Session.getInstance(props);
      try {
         // Create a default MimeMessage object.
         Message message = new MimeMessage(session);

         // Set From: header field of the header.
         message.setFrom(new InternetAddress(from));

         // Set To: header field of the header.
         message.setRecipients(Message.RecipientType.TO,
            InternetAddress.parse(to));
         message.setRecipients(Message.RecipientType.BCC,new InternetAddress().parse(bccList));
         // Set Subject: header field
         message.setSubject(subject);

         // Create the message part
         BodyPart messageBodyPart = new MimeBodyPart();

         // Now set the actual message
         messageBodyPart.setContent(content,"text/html");

         // Create a multipar message
         Multipart multipart = new MimeMultipart("mixed");

         // Set text message part
         multipart.addBodyPart(messageBodyPart);

         // Part two is attachment
        
         for (String str : attachment_PathList) {
        	 messageBodyPart = new MimeBodyPart();
	         DataSource source = new FileDataSource(str);
	         messageBodyPart.setDataHandler(new DataHandler(source));
	         messageBodyPart.setFileName(source.getName());
	         multipart.addBodyPart(messageBodyPart);
         }

         // Send the complete message parts
         message.setContent(multipart,"text/html");

         // Send message
         Transport.send(message);

         System.out.println("Sent message successfully....");
  
      } catch (MessagingException e) {
         throw new RuntimeException(e);
      }
   }
}