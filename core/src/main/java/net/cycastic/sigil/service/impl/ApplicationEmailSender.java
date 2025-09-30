package net.cycastic.sigil.service.impl;

import net.cycastic.sigil.configuration.mail.ApplicationEmailConfigurations;
import net.cycastic.sigil.service.DecryptionProvider;
import net.cycastic.sigil.service.email.EmailImage;
import net.cycastic.sigil.service.impl.email.EmailSenderImpl;
import jakarta.annotation.Nullable;

import java.util.Map;

public class ApplicationEmailSender extends EmailSenderImpl {
    private final String senderAddress;

    public ApplicationEmailSender(ApplicationEmailConfigurations configurations, DecryptionProvider decryptionProvider){
        super(getConfigs(configurations, decryptionProvider));
        senderAddress = configurations.getSender();
    }

    private static ApplicationEmailConfigurations getConfigs(ApplicationEmailConfigurations configurations, DecryptionProvider decryptionProvider){
        if (configurations.getEncryptedPassword() != null){
            var decryptedPassword = decryptionProvider.decrypt(configurations.getEncryptedPassword());
            configurations.setPassword(decryptedPassword);
        }

        return configurations;
    }

    public void sendHtml(String to, String cc, String subject, String htmlBody, @Nullable Map<String, EmailImage> imageStreamSource){
        sendHtml(senderAddress, "Sigil", to, cc, subject, htmlBody, imageStreamSource);
    }
}
