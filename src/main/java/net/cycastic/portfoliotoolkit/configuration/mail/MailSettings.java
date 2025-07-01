package net.cycastic.portfoliotoolkit.configuration.mail;

import lombok.Data;

@Data
public class MailSettings {
    private String host;
    private int port;
    private String username;
    private String sender;
    private String password;
    private boolean auth;
    private boolean starttls;
}
