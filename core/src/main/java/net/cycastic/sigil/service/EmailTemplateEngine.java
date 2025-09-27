package net.cycastic.sigil.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.dto.EmailParameterDto;
import net.cycastic.sigil.domain.dto.EmailParameterType;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public interface EmailTemplateEngine {
    @Getter
    @AllArgsConstructor
    class RenderResult {
        private Map<String, EmailImage> imageStreamSource;
    }

    private static HashMap<String, Object> buildParameterMap(EmailParameterDto[] emailParameterDtos){
        HashMap<String, Object> map = HashMap.newHashMap(emailParameterDtos.length);
        for (var parameter: emailParameterDtos){
            if (map.containsKey(parameter.getName())){
                throw RequestException.withExceptionCode("C400T001", parameter.getName());
            }

            if (parameter.getType() == EmailParameterType.DECIMAL){
                try {
                    map.put(parameter.getName(), Double.parseDouble(parameter.getValue()));
                } catch (NumberFormatException e){
                    throw RequestException.withExceptionCode("C400T002", parameter.getValue());
                }

                continue;
            }

            map.put(parameter.getName(), parameter.getValue());
        }
        return map;
    }

    RenderResult render(InputStream templateStream,
                OutputStream renderStream,
                Map<String, Object> emailParameters);

    default RenderResult render(InputStream templateStream, OutputStream renderStream, EmailParameterDto[] emailParameterDtos){
        var map = buildParameterMap(emailParameterDtos);
        return render(templateStream, renderStream, map);
    }
}
