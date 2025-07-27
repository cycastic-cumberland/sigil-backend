package net.cycastic.sigil.domain;

import net.cycastic.sigil.service.EmailImage;
import org.springframework.core.io.InputStreamSource;

import java.io.InputStream;

public class ApplicationAssets {
    public static class EmailImages {
        private static InputStream loadLogoImage(){
            return ApplicationAssets.class.getClassLoader().getResourceAsStream("static/logo.png");
        }

        public static final EmailImage LOGO_EMAIL_IMAGE = new EmailImage() {
            @Override
            public String getFileName() {
                return "logo.png";
            }

            @Override
            public String getMimeType() {
                return "image/png";
            }

            @Override
            public InputStreamSource getImageSource() {
                return EmailImages::loadLogoImage;
            }
        };
    }
}
