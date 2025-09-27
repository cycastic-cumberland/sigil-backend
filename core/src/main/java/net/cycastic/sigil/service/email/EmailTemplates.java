package net.cycastic.sigil.service.email;

import java.io.InputStream;

public class EmailTemplates {
    public static InputStream registrationCompletion() {
        return getTemplate("templates/register/RegistrationCompletionMail.ftl");
    }

    public static InputStream tenantUserInvitation() {
        return getTemplate("templates/tenant/invite/InvitationMail.ftl");
    }

    private static InputStream getTemplate(String resourcePath){
        return EmailTemplates.class.getClassLoader().getResourceAsStream(resourcePath);
    }
}
