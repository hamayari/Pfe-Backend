package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.List;
import jakarta.mail.*;
import jakarta.mail.search.FlagTerm;
import org.springframework.beans.factory.annotation.Value;
import java.util.ArrayList;
import java.util.Properties;
import java.io.InputStream;

@Service
public class EmailAttachmentService {
    @Value("${mail.imap.host}")
    private String imapHost;
    @Value("${mail.imap.port}")
    private int imapPort;
    @Value("${mail.imap.username}")
    private String username;
    @Value("${mail.imap.password}")
    private String password;
    @Value("${mail.imap.folder:INBOX}")
    private String folderName;

    public List<EmailAttachment> fetchNewPaymentProofs() {
        List<EmailAttachment> attachments = new ArrayList<>();
        Properties props = new Properties();
        props.put("mail.store.protocol", "imap");
        props.put("mail.imap.host", imapHost);
        props.put("mail.imap.port", String.valueOf(imapPort));
        try {
            Session session = Session.getInstance(props);
            Store store = session.getStore("imap");
            store.connect(imapHost, imapPort, username, password);
            Folder folder = store.getFolder(folderName);
            folder.open(Folder.READ_WRITE);
            // Récupérer les messages non lus
            Message[] messages = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            for (Message message : messages) {
                if (message.getContentType().contains("multipart")) {
                    Multipart multipart = (Multipart) message.getContent();
                    for (int i = 0; i < multipart.getCount(); i++) {
                        BodyPart part = multipart.getBodyPart(i);
                        String disposition = part.getDisposition();
                        if (disposition != null && (disposition.equalsIgnoreCase(Part.ATTACHMENT) || disposition.equalsIgnoreCase(Part.INLINE))) {
                            String fileName = part.getFileName();
                            String contentType = part.getContentType();
                            if (fileName != null && (fileName.toLowerCase().endsWith(".pdf") || fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg") || fileName.toLowerCase().endsWith(".png"))) {
                                InputStream is = part.getInputStream();
                                // Create a simple concrete implementation
                                final String finalFileName = fileName;
                                final String finalContentType = contentType;
                                final InputStream finalIs = is;
                                @SuppressWarnings("unused")
                                final String finalMessageId = message.getHeader("Message-ID") != null ? message.getHeader("Message-ID")[0] : null;
                                @SuppressWarnings("unused")
                                final String finalSender = message.getFrom() != null ? message.getFrom()[0].toString() : null;
                                @SuppressWarnings("unused")
                                final String finalSubject = message.getSubject();
                                @SuppressWarnings("unused")
                                final java.util.Date finalReceivedDate = message.getReceivedDate();
                                
                                EmailAttachment att = new EmailAttachment() {
                                    @Override
                                    public InputStream getInputStream() { return finalIs; }
                                    @Override
                                    public String getFileName() { return finalFileName; }
                                    @Override
                                    public String getContentType() { return finalContentType; }
                                    @Override
                                    public long getSize() { return 0; } // Default size
                                };
                                attachments.add(att);
                            }
                        }
                    }
                }
                // Marquer comme lu
                message.setFlag(Flags.Flag.SEEN, true);
            }
            folder.close(false);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return attachments;
    }
} 