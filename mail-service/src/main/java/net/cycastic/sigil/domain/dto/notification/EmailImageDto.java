package net.cycastic.sigil.domain.dto.notification;

import lombok.Data;
import net.cycastic.sigil.service.email.EmailImage;
import org.springframework.core.io.InputStreamSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;

@Data
public class EmailImageDto implements EmailImage {
    private String fileName;
    private String mimeType;
    private String content;

    private InputStream getContentStream(){
        return new ByteArrayInputStream(Base64.getDecoder().decode(content));
    }

    public InputStreamSource getImageSource(){
        return this::getContentStream;
    }

//    public static Map<String, EmailImageDto> from
}
