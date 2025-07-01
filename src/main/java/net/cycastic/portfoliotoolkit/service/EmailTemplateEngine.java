package net.cycastic.portfoliotoolkit.service;

import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.model.EmailParameter;
import net.cycastic.portfoliotoolkit.domain.model.EmailParameterType;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public interface EmailTemplateEngine {
    private static HashMap<String, Object> buildParameterMap(EmailParameter[] emailParameters){
        HashMap<String, Object> map = HashMap.newHashMap(emailParameters.length);
        for (var parameter: emailParameters){
            if (map.containsKey(parameter.getName())){
                throw new RequestException(400, "Email parameter %s already declared", parameter.getName());
            }

            if (parameter.getType() == EmailParameterType.DECIMAL){
                try {
                    map.put(parameter.getName(), Double.parseDouble(parameter.getValue()));
                } catch (NumberFormatException e){
                    throw new RequestException(400, "Undefined decimal format: %s", parameter.getValue());
                }

                continue;
            }

            map.put(parameter.getName(), parameter.getValue());
        }
        return map;
    }

    void render(InputStream templateStream,
                OutputStream renderStream,
                Map<String, Object> emailParameters);

    default void render(InputStream templateStream, OutputStream renderStream, EmailParameter[] emailParameters){
        var map = buildParameterMap(emailParameters);
        render(templateStream, renderStream, map);
    }
}
