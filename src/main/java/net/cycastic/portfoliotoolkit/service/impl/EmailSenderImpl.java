package net.cycastic.portfoliotoolkit.service.impl;

import jakarta.mail.internet.InternetAddress;
import lombok.SneakyThrows;
import net.cycastic.portfoliotoolkit.configuration.mail.MailSettings;
import net.cycastic.portfoliotoolkit.service.EmailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

public class EmailSenderImpl implements EmailSender {
    private final JavaMailSender sender;

    public EmailSenderImpl(MailSettings mailSettings){
        sender = createSender(mailSettings);
    }

    private static JavaMailSender createSender(MailSettings mailSettings) {
        var sender = new JavaMailSenderImpl();
        sender.setHost(mailSettings.getHost());
        sender.setPort(mailSettings.getPort());
        sender.setUsername(mailSettings.getUsername());
        sender.setPassword(mailSettings.getPassword());

        var props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", String.valueOf(mailSettings.isAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(mailSettings.isStarttls()));

        return sender;
    }

    @Override
    @SneakyThrows
    public void sendHtml(String fromAddress, String fromName, String to, String cc, String subject, String htmlBody) {
        var message = sender.createMimeMessage();
        var helper = new MimeMessageHelper(message, true);

        helper.setFrom(new InternetAddress(fromAddress, fromName));
        helper.setTo(to);
        if (cc != null){
            helper.setCc(cc);
        }
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        sender.send(message);
    }
}
