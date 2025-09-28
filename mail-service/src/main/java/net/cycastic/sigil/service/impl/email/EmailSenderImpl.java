package net.cycastic.sigil.service.impl.email;

import jakarta.mail.internet.InternetAddress;
import lombok.SneakyThrows;
import net.cycastic.sigil.configuration.mail.MailSettings;
import net.cycastic.sigil.service.email.EmailImage;
import net.cycastic.sigil.service.email.EmailSender;
import jakarta.annotation.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.Map;

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
        props.put("mail.mime.charset", "UTF-8");
        props.put("mail.smtp.allowutf8", "true");

        return sender;
    }

    @Override
    @SneakyThrows
    public void sendHtml(String fromAddress, String fromName, String to, String cc, String subject, String htmlBody, @Nullable Map<String, ? extends EmailImage> imageStreamSource) {
        var message = sender.createMimeMessage();
        var helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(new InternetAddress(fromAddress, fromName));
        helper.setTo(to);
        if (cc != null){
            helper.setCc(cc);
        }
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        if (imageStreamSource != null){
            for (var entry : imageStreamSource.entrySet()){
                helper.addInline(entry.getKey(), entry.getValue().getImageSource(), entry.getValue().getMimeType());
            }
        }

        sender.send(message);
    }
}
